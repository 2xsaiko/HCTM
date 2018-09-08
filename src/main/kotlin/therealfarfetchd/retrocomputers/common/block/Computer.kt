package therealfarfetchd.retrocomputers.common.block

import mcmultipart.api.slot.EnumFaceSlot
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import therealfarfetchd.quacklib.common.api.extensions.*
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer
import therealfarfetchd.retrocomputers.common.api.cpu.IMemoryProvider
import therealfarfetchd.retrocomputers.common.api.cpu.IProcessor
import therealfarfetchd.retrocomputers.common.api.item.IProcessorFactory
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal

/**
 * Created by marco on 02.07.17.
 */
@BlockDef(creativeTab = ModID)
class Computer : Horizontal(0), ITickable {

  private val memory: ByteArray = ByteArray(8192)
  var diskAddr: Byte = 2
  var termAddr: Byte = 1
  var running: Boolean = false
  private var busTrg: Byte = 0
  private var rwAddr: Short = 0
  var rwEnabled: Boolean = false; private set

  private var cpuItem: ItemStack = ItemStack.EMPTY
  var cpu: IProcessor? = null; private set

  private var backplane: Array<Memory?> = arrayOfNulls(7)

  private var ibuf: Int = 0

  val cpuTimeout
    get() = cpu?.timeout ?: true
  val cpuError
    get() = cpu?.error ?: true

  var clientBCon = false
  var clientBFai = false
  var clientCpu = false
  var clientTimeout = false
  var clientError = false

  val provider = object : IMemoryProvider {
    var bus: BusDataContainer? = null
    override var busFailed: Boolean = false

    override fun get(s: Short): Byte {
      val addr = s.unsigned
      return if (addr < 8192) memory[addr]
      else backplane[addr / 8192 - 1]?.let { it.mem[addr % 8192] } ?: 0
    }

    override fun set(s: Short, b: Byte) {
      val addr = s.unsigned
      if (addr < 8192) memory[addr] = b
      else backplane[addr / 8192 - 1]?.also { it.mem[addr % 8192] = b; it.dataChanged() }
    }

    override val termAddr: Byte
      get() = this@Computer.termAddr

    override val diskAddr: Byte
      get() = this@Computer.diskAddr

    override var targetBus: Byte
      get() = this@Computer.busTrg
      set(value) {
        this@Computer.busTrg = value
      }

    override val isBusConnected: Boolean
      get() = bus != null

    override fun bus(): BusDataContainer? {
      return if (!busFailed) {
        if (bus == null)
          bus = data.resolveNetwork().mapNotNull { it as? BusDataContainer }.find { it.busId == targetBus }
        if (bus == null) busFailed = true
        bus
      } else null
    }

    override fun resetBusState() {
      bus = null
      busFailed = false
    }

    override var allowWrite: Boolean
      get() = rwEnabled
      set(value) {
        rwEnabled = value
      }

    override var writePos: Short
      get() = rwAddr
      set(value) {
        rwAddr = value
      }

    override fun halt() {
      running = false
    }

  }

  private val clientCL = ChangeListener(this::diskAddr, this::termAddr, this::running, this::rwEnabled,
    provider::isBusConnected, provider::busFailed, this::cpuTimeout, this::cpuError)
  private val worldCL = ChangeListener(this::memory, this::diskAddr, this::termAddr, this::running, this::busTrg,
    this::rwAddr, this::rwEnabled, this::cpuItem, this::cpu, this::backplane, this::ibuf)

  override fun onActivated(player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
    if (facing == this.facing.opposite && player.isSneaking && !cpuItem.isEmpty) {
      if (world.isServer) {
        val (x, y, z) = Vec3d(pos) + Vec3d(0.5, 0.5, 0.5) + Vec3d(facing.directionVec) * 0.5
        val (vx, vy, vz) = Vec3d(facing.directionVec) * 0.1
        cpuItem.spawnAt(world, x, y, z, vx, vy, vz)
        cpuItem = ItemStack.EMPTY
      }
      return true
    }
    val item = player.getHeldItem(hand)
    if (facing == this.facing.opposite && cpuItem.isEmpty && item.item is IProcessorFactory<*>) {
      if (world.isServer) {
        cpuItem = item.copy().also { it.count = 1 }
        item.count--
      }
      return true
    }
    return openGui(player)
  }

  override fun onBreakBlock() {
    super.onBreakBlock()
    cpuItem.spawnAt(world, pos)
  }

  override fun update() {
    if (world.isServer) {
      provider.resetBusState()
      syncCpu()
      if (running) {
        val cpu = cpu
        if (cpu == null) {
          running = false
        } else {
          refreshBackplanes()
          cpu.timeout = false
          ibuf = minOf(cpu.insnBufferSize, ibuf + cpu.insnGain)
          while (ibuf > 0 && !cpu.timeout && running) {
            try {
              cpu.next()
            } catch (e: Exception) {
              e.printStackTrace()
              cpu.timeout = true
            }
            ibuf--
          }
        }
      }
    }
    if (worldCL.valuesChanged()) dataChanged()
    if (clientCL.valuesChanged()) clientDataChanged(false)
  }

  private fun refreshBackplanes() {
    for (i in backplane.indices) backplane[i] = null
    for (i in backplane.indices) {
      val p = pos.offset(facing.opposite, i + 1)

      // abort when there's no backplane anymore
      world.getQBlock(p, EnumFaceSlot.DOWN) as? Backplane ?: break

      val mem = world.getQBlock(p, EnumFaceSlot.UP) as? Memory
      backplane[i] = mem
    }
  }

  private fun syncCpu() {
    if (cpuItem.isEmpty) {
      cpu = null
      ibuf = 0
    } else {
      val f = cpuItem.item as IProcessorFactory<*>
      val cpu1 = cpu
      if (cpu1 == null || f.processorType.canonicalName != cpu1.javaClass.canonicalName) {
        val c = f(cpuItem)
        c.mem = provider
        c.reset(true)
        cpu = c
        ibuf = 0
      }
    }
  }

  override fun peek(addr: Byte): Byte {
    return if (rwEnabled) memory[(rwAddr.unsigned + addr.unsigned) and 0xFFFF] else 0
  }

  override fun poke(addr: Byte, b: Byte) {
    if (rwEnabled) memory[(rwAddr.unsigned + addr.unsigned) and 0xFFFF] = b
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    syncCpu()
    with(nbt) {
      bool["Running"] = running
      byte["TermAddr"] = termAddr
      byte["DiskAddr"] = diskAddr
      bool["RWEnabled"] = rwEnabled
      if (target == DataTarget.Save) {
        bytes["Memory"] = memory
        byte["BusTrg"] = busTrg
        short["RWAddr"] = rwAddr
        item["CPUItem"] = cpuItem
        cpu?.also { it.saveData(this.nbt["CPU"]) }
      }
      if (target == DataTarget.Client) {
        byte["f"] = packByte(
          provider.isBusConnected,
          provider.busFailed,
          cpu != null,
          cpuTimeout,
          cpuError
        )
      }
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    with(nbt) {
      running = bool["Running"]
      termAddr = byte["TermAddr"]
      diskAddr = byte["DiskAddr"]
      rwEnabled = bool["RWEnabled"]
      if (target == DataTarget.Save) {
        bytes["Memory"].copyTo(memory)
        busTrg = byte["BusTrg"]
        rwAddr = short["RWAddr"]
        cpuItem = item["CPUItem"]
        syncCpu()
        cpu?.also { it.loadData(this.nbt["CPU"]) }
      }
      if (target == DataTarget.Client) {
        val (bc, bf, cpu, to, err) = unpack(byte["f"])
        clientBCon = bc
        clientBFai = bf
        clientCpu = cpu
        clientTimeout = to
        clientError = err
      }
    }
  }

  override val material: Material = Material.IRON

  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "computer")

  companion object {
    val Block by WrapperImplManager.container(Computer::class)
    val Item by WrapperImplManager.item(Computer::class)
  }
}