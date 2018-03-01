package therealfarfetchd.rswires.common.block

import net.minecraft.block.SoundType
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.model.wire.WireModel
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.api.block.IRedstoneConductor
import therealfarfetchd.rswires.common.api.block.RedstoneWireType
import therealfarfetchd.rswires.common.api.block.TypeBundled
import therealfarfetchd.rswires.common.api.block.TypeInsulated
import therealfarfetchd.rswires.common.util.EnumBundledWireColor

@BlockDef(
  registerModels = false,
  creativeTab = ModID,
  metaModels = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
)
class BundledCable : RSBaseWire<EnumDyeColor>(0.375, 0.25) {
  var color: EnumBundledWireColor = EnumBundledWireColor.None

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    color = EnumBundledWireColor.values()[stack?.metadata ?: 0]
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
  }

  override fun checkAdditional(cap: Any?, localCap: Any?): Boolean {
    return if (cap is IRedstoneConductor && localCap is IRedstoneConductor) {
      when (cap.wireType) {
        is TypeInsulated                       -> true
        TypeBundled(color),
        TypeBundled(EnumBundledWireColor.None) -> true
        else                                   -> cap.wireType is TypeBundled && color == EnumBundledWireColor.None
      }
    } else false
  }

  override fun getInput(channel: EnumDyeColor): Boolean = false

  override fun mapChannel(otherType: RedstoneWireType, otherChannel: Any?) = when (otherType) {
    is TypeBundled   -> otherChannel as EnumDyeColor
    is TypeInsulated -> otherType.color
    else             -> error("Wrong turn, buddy.")
  }

  override fun saveChannelData(nbt: QNBTCompound) {
    nbt.ushort["s"] = when {
      active.isNotEmpty() -> EnumDyeColor.values()
        .filter(active::contains)
        .map { 1 shl it.metadata }
        .reduce(Int::or)
      else                -> 0
    }
  }

  override fun loadChannelData(nbt: QNBTCompound) {
    val s = nbt.ushort["s"]
    active = EnumDyeColor.values().filter { ((1 shl it.metadata) and s) != 0 }.toSet()
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["c"] = color.ordinal
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    color = EnumBundledWireColor.values()[nbt.ubyte["c"]]
  }

  override fun applyProperties(state: IBlockState) =
    super.applyProperties(state).withProperty(PropColor, color)

  override fun getValidChannels(): Set<EnumDyeColor> = EnumDyeColor.values().toSet()
  override fun getWireType() = TypeBundled(color)
  override fun filterChannel(otherType: RedstoneWireType, otherChannel: Any?) = true
  override fun getItem(): ItemStack = Item.makeStack()

  override val properties: Set<IProperty<*>> = super.properties + PropColor
  override val blockType: ResourceLocation = ResourceLocation(ModID, "bundled_cable")
  override val soundType: SoundType = SoundType.CLOTH

  companion object {
    val Block by WrapperImplManager.container(BundledCable::class)
    val Item by WrapperImplManager.item(BundledCable::class)

    init {
      WrapperImplManager.itemMod(BundledCable::class) {
        hasSubtypes = true
        maxDamage = 0
      }
    }

    val Textures = EnumBundledWireColor.values().map { it to ResourceLocation("$ModID:blocks/bundled_cable/${it.getName()}") }.toMap()

    val Bakery = WireModel(0.375, 0.25, 32.0, Textures.map { it.value },
      { Textures[it[PropColor]]!! },
      { Textures[EnumBundledWireColor.byMetadata(it.itemDamage)]!! }
    )

    val PropColor = PropertyEnum.create("color", EnumBundledWireColor::class.java)
  }
}