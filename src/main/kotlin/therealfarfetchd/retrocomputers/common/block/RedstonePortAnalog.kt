package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.extensions.unsigned
import therealfarfetchd.quacklib.common.api.qblock.IQBlockRedstone
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.capability.SlabConnectable
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal

@BlockDef(registerModels = false, creativeTab = ModID)
class RedstonePortAnalog : Horizontal(3), IQBlockRedstone {
  private var inputLevel: Int = 0
  private var outputLevel: Int = 0

  val connectable = SlabConnectable(data, { false })

  override fun canConnect(side: EnumFacing): Boolean = side == facing

  override fun getOutput(side: EnumFacing, strong: Boolean): Int = if (canConnect(side) && !strong) outputLevel else 0

  override fun peek(addr: Byte): Byte {
    return when (addr.unsigned) {
      0 -> inputLevel.toByte()
      2 -> outputLevel.toByte()
      else -> 0
    }
  }

  override fun poke(addr: Byte, b: Byte) {
    when (addr.unsigned) {
      0, 2 -> outputLevel = minOf(15, b.unsigned)
    }
    dataChanged()
    world.neighborChanged(pos.offset(facing.opposite), Block, pos)
    clientDataChanged()
  }

  private fun updateRedstoneInput() {
    inputLevel = world.getRedstonePower(pos.offset(facing.opposite), facing.opposite)
  }

  override fun onAdded() {
    super.onAdded()
    updateRedstoneInput()
  }

  override fun onNeighborChanged(side: EnumFacing) {
    super.onNeighborChanged(side)
    updateRedstoneInput()
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["In"] = inputLevel
    nbt.ubyte["Out"] = outputLevel
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    inputLevel = nbt.ubyte["In"]
    outputLevel = nbt.ubyte["Out"]
  }

  override fun connectionForSide(f: EnumFacing?): IConnectable? {
    return when {
      f == null -> super.connectionForSide(EnumFacing.DOWN)
      f == EnumFacing.DOWN -> super.connectionForSide(f)
      f == facing.opposite -> null
      f.axis == EnumFacing.Axis.Y -> null
      else -> connectable
    }
  }

  override fun applyProperties(state: IBlockState): IBlockState =
    super.applyProperties(state).withProperty(PropOutput, outputLevel)

  override fun getItem(): ItemStack = Item.makeStack()

  override val collisionBox: Collection<AxisAlignedBB>
    get() = BoundingBox

  override val properties: Set<IProperty<*>> = super.properties + PropOutput
  override val isFullBlock: Boolean = false
  override val material: Material = Material.IRON
  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT
  override val blockType: ResourceLocation = ResourceLocation(ModID, "redstone_port_analog")

  companion object {
    val PropOutput = PropertyInteger.create("output", 0, 15)

    val BoundingBox = setOf(AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0))

    val Block by WrapperImplManager.container(RedstonePortAnalog::class)
    val Item by WrapperImplManager.item(RedstonePortAnalog::class)
  }
}