package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3i
import therealfarfetchd.quacklib.common.api.extensions.*
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.util.math.Random
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal
import therealfarfetchd.retrocomputers.common.ether.EtherSource
import therealfarfetchd.retrocomputers.common.ether.IEtherEntry
import therealfarfetchd.retrocomputers.common.ether.RadioEther
import java.util.*

/**
 * Created by marco on 10.07.17.
 *
 * Address map:
 * $00-$7F: data buffer (read/write)
 * $80-$81: Sender ID (randomly generated)
 * $82: Command (0: no action, 1: fill read buffer (listen), 2: send to specified ID, 3: broadcast, -1: error)
 * $83-$84: Target ID (send), Recieve ID (listen)
 * $85: response count (listen)
 * $86: selected response (changes data buffer read and distance)
 * $87-$8A: Distance (float type)
 */
@BlockDef(creativeTab = ModID)
class Radio : Horizontal(4), ITickable, IEtherEntry {
  private val nullPacket = REPacket(0, 0, ByteArray(128, { 0 }), 0.0f)

  private val dataBuf: ByteArray = ByteArray(0x80)

  private var txAct: Int = 0
  private var radioId: Short = Random.nextShort()
  private var command: Byte = 0
  private var targetId: Short = 0
  private var packetqueue: List<REPacket> = emptyList()
  private val packets: Array<REPacket> = Array(255, { nullPacket })
  private var selection: Byte = 0

  private val clientCL = ChangeListener(this::txAct, { this.packetqueue.isEmpty() })
  private val worldCL = ChangeListener(this::dataBuf, this::txAct, this::radioId, this::command, this::targetId, this::packetqueue, this::packets, this::selection)

  // client render
  private var rxLight = false
  private var txLight = false

  override val properties: Set<IProperty<*>> = super.properties + PropState

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state)
      .withProperty(PropState, if (rxLight) StateRx else 0 + if (txLight) StateTx else 0)
  }

  override fun update() {
    if (world.isServer) {
      RadioEther.addEntry(this)
      packetqueue = packetqueue.filterNot { it.isExpired }
      if (txAct != 0) txAct -= 1
      when (command.toInt() and 0xFF) {
        1 -> {
          // fill read buffer
          packets.fill(nullPacket)
          packetqueue.copyTo(packets)
          packetqueue = packetqueue.drop(packets.size)
          selection = 0
          command = 0
        }
        2 -> {
          // send
          RadioEther.send(EtherSource(pos, world.provider.dimension, radioId), dataBuf.toList(), targetId)
          txAct = 3
          command = 0
        }
        3 -> {
          // broadcast
          RadioEther.broadcast(EtherSource(pos, world.provider.dimension, radioId), dataBuf.toList())
          txAct = 3
          command = 0
        }
        0 -> Unit
        else -> command = -1
      }
      if (worldCL.valuesChanged()) dataChanged()
      if (clientCL.valuesChanged()) clientDataChanged()
    }
  }

  override fun peek(addr: Byte): Byte {
    val uaddr = addr.unsigned
    return when (uaddr) {
      in 0x00..0x7F -> {
        val sel = selection.unsigned
        if (sel < packets.size) packets[sel].data[uaddr]
        else 0
      }
      0x80 -> radioId.toByte()
      0x81 -> (radioId shr 8).toByte()
      0x82 -> command
      0x83 -> packets[selection.unsigned].srcId.toByte()
      0x84 -> (packets[selection.unsigned].srcId shr 8).toByte()
      0x85 -> packets.count { it != nullPacket }.toByte()
      0x86 -> selection
      in 0x87..0x8A -> {
        val sel = selection.unsigned
        if (sel < packets.size) (packets[sel].dist.toIntBits() shr ((0x87 - uaddr) * 8)).toByte()
        else 0
      }
      else -> 0
    }
  }

  override fun poke(addr: Byte, b: Byte) {
    val uaddr = addr.unsigned
    when (uaddr) {
      in 0x00..0x7F -> dataBuf[uaddr] = b
      0x80 -> radioId = ((radioId.unsigned and 0xFF00) or b.unsigned).toShort()
      0x81 -> radioId = ((radioId.unsigned and 0x00FF) or (b.unsigned shl 8)).toShort()
      0x82 -> command = b
      0x83 -> targetId = ((targetId.unsigned and 0xFF00) or b.unsigned).toShort()
      0x84 -> targetId = ((targetId.unsigned and 0x00FF) or (b.unsigned shl 8)).toShort()
      0x86 -> selection = b
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    when (target) {
      DataTarget.Client -> {
        nbt.byte["V"] = packByte(txAct != 0, packetqueue.isNotEmpty())
      }
      DataTarget.Save -> {
        nbt.int["Tx"] = txAct
        nbt.bytes["Buf"] = dataBuf
        nbt.short["LId"] = radioId
        nbt.short["RId"] = targetId
        nbt.byte["Cmd"] = command
        nbt.byte["Sel"] = selection
        nbt.nbts["Q"] = packetqueue.map { it.serializeNBT() }
        nbt.nbts["PLst"] = packets.map { it.serializeNBT() }
      }
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    when (target) {
      DataTarget.Client -> {
        val f = unpack(nbt.byte["V"])
        txLight = f[0]
        rxLight = f[1]
      }
      DataTarget.Save -> {
        txAct = nbt.int["Tx"]
        nbt.bytes["Buf"].copyTo(dataBuf)
        radioId = nbt.short["LId"]
        targetId = nbt.short["RId"]
        command = nbt.byte["Cmd"]
        selection = nbt.byte["Sel"]
        packetqueue = nbt.nbts["Q"].map { REPacket(it) }
        nbt.nbts["PLst"].map { REPacket(it) }.copyTo(packets)
      }
    }
  }

  override fun receiveData(src: EtherSource, data: List<Byte>) {
    packetqueue += REPacket(world.totalWorldTime, src.id, data.toByteArray(), src.pos, src.dimension)
  }

  override val id: Short
    get() = radioId

  override val isInvalid: Boolean
    get() = container.isInvalid

  override val material: Material = Material.IRON

  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "radio")

  private inner class REPacket(val currentTime: Long, val srcId: Short, val data: ByteArray, val dist: Float) {
    constructor(currentTime: Long, srcId: Short, data: ByteArray, origin: Vec3i, dim: Int) :
      this(currentTime, srcId, data, if (world.provider.dimension == dim) (pos distanceTo origin).toFloat() else 0.0f)

    constructor(nbt: QNBTCompound) : this(nbt.long["Time"], nbt.short["SrcId"], nbt.bytes["Data"], nbt.float["Dist"])

    val isExpired: Boolean
      get() = world.totalWorldTime - currentTime > 200

    fun serializeNBT(): QNBTCompound {
      val nbt = QNBTCompound()
      nbt.long["Time"] = currentTime
      nbt.short["SrcID"] = srcId
      nbt.bytes["Data"] = data
      nbt.float["Dist"] = dist
      return nbt
    }

    override fun equals(other: Any?): Boolean {
      return (other is REPacket) &&
             other.currentTime == currentTime &&
             other.srcId == srcId &&
             Arrays.equals(other.data, data) &&
             other.dist == dist
    }

    override fun hashCode(): Int {
      var result = currentTime.hashCode()
      result = 31 * result + srcId
      result = 31 * result + Arrays.hashCode(data)
      result = 31 * result + dist.hashCode()
      return result
    }
  }

  companion object {
    val StateRx = 1
    val StateTx = 2

    val PropState = PropertyInteger.create("state", 0, 3)!!

    val Block by WrapperImplManager.container(Radio::class)
    val Item by WrapperImplManager.item(Radio::class)
  }
}