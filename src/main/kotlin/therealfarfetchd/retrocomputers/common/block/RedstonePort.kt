package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.property.IExtendedBlockState
import net.minecraftforge.common.property.IUnlistedProperty
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.extensions.plus
import therealfarfetchd.quacklib.common.api.extensions.unsigned
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.capability.RedstonePortConnectable
import therealfarfetchd.retrocomputers.common.block.capability.SlabConnectable
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal
import therealfarfetchd.rswires.common.api.block.BundledConductor
import therealfarfetchd.rswires.common.util.NetworkPropagator

/**
 * Created by marco on 10.07.17.
 */
@BlockDef(registerModels = false, creativeTab = ModID, dependencies = "[bundled cable]")
class RedstonePort : Horizontal(3) {
  private var inputLevel: Int = 0
  private var outputLevel: Int = 0
  var top: Boolean = false; private set

  val rsCap = BundledConductor(neighborSupport({ it.base == facing.opposite }), this::getOutput, this::setInput)

  val connectable = SlabConnectable(data, { top })
  val backConnectable = RedstonePortConnectable({ top }, rsCap)

  fun beforePlace(sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    top = when (sidePlaced) {
      EnumFacing.DOWN -> hitY >= 0.5f
      else -> hitY > 0.5f
    }
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    beforePlace(sidePlaced, hitX, hitY, hitZ)
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
  }

  private fun getOutput(color: EnumDyeColor): Boolean = (outputLevel shr color.metadata) and 1 != 0

  private fun setInput(color: EnumDyeColor, b: Boolean) {
    val bitPos = 1 shl color.metadata
    val mask = 0xFFFF xor bitPos
    inputLevel = (inputLevel and mask) or ((if (b) -1 else 0) and bitPos)
  }

  override fun peek(addr: Byte): Byte {
    return when (addr.unsigned) {
      0 -> inputLevel.toByte()
      1 -> (inputLevel shr 8).toByte()
      2 -> outputLevel.toByte()
      3 -> (outputLevel shr 8).toByte()
      else -> 0
    }
  }

  override fun poke(addr: Byte, b: Byte) {
    when (addr.unsigned) {
      0, 2 -> outputLevel = (outputLevel and 0xFF00) or b.unsigned
      1, 3 -> outputLevel = (outputLevel and 0x00FF) or (b.unsigned shl 8)
    }
    dataChanged()
    clientDataChanged()
    for (c in EnumDyeColor.values())
      NetworkPropagator.schedulePropagate(rsCap, c)
  }

  override fun connectionForSide(f: EnumFacing?): IConnectable? {
    return when {
      f == null -> super.connectionForSide(EnumFacing.DOWN)
      f == if (top) EnumFacing.UP else EnumFacing.DOWN -> super.connectionForSide(f)
      f.axis == EnumFacing.Axis.Y -> null
      f == facing.opposite -> backConnectable
      else -> connectable
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.bool["top"] = top
    nbt.ushort["o"] = outputLevel
    if (target == DataTarget.Save)
      nbt.ushort["i"] = inputLevel
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    top = nbt.bool["top"]
    outputLevel = nbt.ushort["o"]
  }

  override fun applyProperties(state: IBlockState): IBlockState =
    super.applyProperties(state).withProperty(PropTop, top)

  override fun applyExtendedProperties(state: IExtendedBlockState): IExtendedBlockState =
    super.applyExtendedProperties(state).withProperty(PropOutput, Integer(outputLevel))

  // override fun getPartSlot(): IPartSlot = if (top) EnumFaceSlot.UP else EnumFaceSlot.DOWN
  override fun getItem(): ItemStack = Item.makeStack()

  override val collisionBox: Collection<AxisAlignedBB>
    get() = if (top) BoundingBoxTop else BoundingBox

  override val properties: Set<IProperty<*>> = super.properties + PropTop
  override val unlistedProperties: Set<IUnlistedProperty<*>> = super.unlistedProperties + PropOutput
  override val isFullBlock: Boolean = false
  override val material: Material = Material.IRON
  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT
  override val blockType: ResourceLocation = ResourceLocation(ModID, "redstone_port")

  companion object {
    val BoundingBox = setOf(AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0))
    val BoundingBoxTop = BoundingBox.map { it + Vec3d(0.0, 0.5, 0.0) }

    val PropTop = PropertyBool.create("top")

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val PropOutput = object : IUnlistedProperty<Integer> {
      override fun getName() = "output"
      override fun isValid(value: Integer?) = value?.toInt() in 0..65535
      override fun valueToString(value: Integer?): String = value.toString()
      override fun getType() = Integer::class.java
    }

    val Block by WrapperImplManager.container(RedstonePort::class)
    val Item by WrapperImplManager.item(RedstonePort::class)
  }
}