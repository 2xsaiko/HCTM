package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
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
import therealfarfetchd.retrocomputers.common.api.IFloppy
import therealfarfetchd.retrocomputers.common.api.item.IFloppyProvider
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal

/**
 * Created by marco on 10.07.17.
 */
@BlockDef(creativeTab = ModID)
class DiskDrive : Horizontal(2), ITickable {
  private var act: Boolean = false
  private var disk: ItemStack = ItemStack.EMPTY
  private val buffer: ByteArray = ByteArray(128)
  private var sector: Short = 0
  private var command: Byte = 0

  // client only - used instead of tracking the entire disk disk since it isn't used in rendering
  private var clientItem: Boolean = false

  private val floppy: IFloppy?; get() = (disk.item as? IFloppyProvider)?.invoke(disk)

  private val clientCL: ChangeListener = ChangeListener(this::act, this::disk)
  private val worldCL: ChangeListener = ChangeListener(this::act, this::disk, this::buffer)

  override fun onActivated(player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
    if (this.facing == facing) {
      if (!disk.isEmpty || clientItem) {
        if (world.isServer) {
          val (x, y, z) = Vec3d(pos) + Vec3d(0.5, 0.5, 0.5) + Vec3d(facing.directionVec) * 0.5
          val (vx, vy, vz) = Vec3d(facing.directionVec) * 0.1
          disk.spawnAt(world, x, y, z, vx, vy, vz)
          disk = ItemStack.EMPTY
        }
        return true
      }
      val heldItem = player.getHeldItem(hand)
      if (!heldItem.isEmpty && heldItem.item is IFloppyProvider) {
        if (world.isServer) {
          val copy = heldItem.copy()
          copy.count = 1
          disk = copy
          heldItem.count--
        }
        return true
      }
    }
    return false
  }

  override fun update() {
    if (world.isServer) {
      if (act) act = false
      if (command > 0) {
        act = true
        var result = 0
        val f = floppy
        if (f == null) result = -1
        else when (command.unsigned) {
          1 -> {
            // get disk name
            for (i in buffer.indices) buffer[i] = 0
            val diskname = f.label.toByteArray(Charsets.US_ASCII)
            for (i in diskname.indices.filter { it < buffer.size }) buffer[i] = diskname[i]
          }
          2 -> {
            // set disk name
            val len = buffer.withIndex().filter { it.value == 0.toByte() }.minBy { it.index }!!.index
            f.label = String(buffer, 0, len, Charsets.US_ASCII)
          }
          3 -> {
            // get disk uuid
            for (i in buffer.indices) buffer[i] = 0
            val diskname = f.uniqueId.toString().toByteArray(Charsets.US_ASCII)
            for (i in diskname.indices.filter { it < buffer.size }) buffer[i] = diskname[i]
          }
          4 -> {
            // read from disk
            if (sector >= 2048) result = -1
            else {
              val s = f.sector[sector]
              if (s == null) result = -1
              else {
                s.copyTo(buffer)
              }
            }
          }
          5 -> {
            // write to disk
            if (sector >= 2048) result = -1
            else {
              f.sector[sector] = buffer.toList()
            }
          }
          6 -> {
            // clear buffer
            for (i in buffer.indices) buffer[i] = 0
          }
        }
        command = result.toByte()
      }
      if (clientCL.valuesChanged()) clientDataChanged()
      if (worldCL.valuesChanged()) dataChanged()
    }
  }

  override fun onBreakBlock() {
    disk.spawnAt(world, pos)
  }

  override fun peek(addr: Byte): Byte {
    return when (addr.unsigned) {
      in 0..127 -> buffer[addr.unsigned]
      128 -> sector.toByte()
      129 -> (sector shr 8).toByte()
      130 -> command
      else -> 0
    }
  }

  override fun poke(addr: Byte, b: Byte) {
    when (addr.unsigned) {
      in 0..127 -> buffer[addr.unsigned] = b
      128 -> sector = ((sector.unsigned and 0xFF00) or b.unsigned).toShort()
      129 -> sector = ((sector.unsigned and 0x00FF) or (b.unsigned shl 8)).toShort()
      130 -> command = b
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Save) {
      nbt.bool["Act"] = act
      nbt.bytes["Buffer"] = buffer
      nbt.short["Sector"] = sector
      nbt.byte["Command"] = command
      nbt.item["Disk"] = disk
    }
    if (target == DataTarget.Client) {
      nbt.byte["f"] = packByte(!disk.isEmpty, act)
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Save) {
      act = nbt.bool["Act"]
      nbt.bytes["Buffer"].copyTo(buffer)
      sector = nbt.short["Sector"]
      command = nbt.byte["Command"]
      disk = nbt.item["Disk"]
    }
    if (target == DataTarget.Client) {
      val (i, l) = unpack(nbt.byte["f"])
      clientItem = i
      act = l
    }
  }

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state).withProperty(PropState, packInt(clientItem, act))
  }

  override val properties: Set<IProperty<*>> = super.properties + PropState

  override val material: Material = Material.IRON

  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "disk_drive")

  companion object {
    val PropState = PropertyInteger.create("state", 0, 3)!!

    val Block by WrapperImplManager.container(DiskDrive::class)
    val Item by WrapperImplManager.item(DiskDrive::class)
  }
}