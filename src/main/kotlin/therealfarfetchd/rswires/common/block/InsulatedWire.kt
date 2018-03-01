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
import therealfarfetchd.quacklib.common.api.extensions.mapWithCopy
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.api.block.*

@BlockDef(
  registerModels = false,
  creativeTab = ModID,
  metaModels = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
)
class InsulatedWire : RSBaseWireSingleChannel(0.25, 0.1875) {
  override fun filterChannel(otherType: RedstoneWireType, otherChannel: Any?) = when (otherType) {
    is TypeBundled -> otherChannel == color
    else           -> true
  }

  var color: EnumDyeColor = EnumDyeColor.WHITE

  override fun getWireType() = TypeInsulated(color)

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    color = EnumDyeColor.byMetadata(stack?.metadata ?: 0)
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
  }

  override fun checkAdditional(cap: Any?, localCap: Any?): Boolean {
    return if (cap is IRedstoneConductor && localCap is IRedstoneConductor) {
      when (cap.wireType) {
        is TypeRedAlloy,
        TypeInsulated(color),
        is TypeBundled -> true
        else           -> false
      }
    } else false
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["c"] = color.metadata
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    color = EnumDyeColor.byMetadata(nbt.ubyte["c"])
  }

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state).withProperty(PropColor, color)
  }

  override fun getItem(): ItemStack = Item.makeStack(meta = color.metadata)

  override val properties: Set<IProperty<*>> = super.properties + PropColor
  override val providePowerToGround: Boolean = false
  override val blockType: ResourceLocation = ResourceLocation(ModID, "insulated_wire")
  override val soundType: SoundType = SoundType.CLOTH

  companion object {
    val Block by WrapperImplManager.container(InsulatedWire::class)
    val Item by WrapperImplManager.item(InsulatedWire::class)

    init {
      WrapperImplManager.itemMod(InsulatedWire::class) {
        hasSubtypes = true
        maxDamage = 0
      }
    }

    val Textures = EnumDyeColor.values()
      .mapWithCopy { color ->
        listOf("on", "off").mapWithCopy { state ->
          ResourceLocation("$ModID:blocks/insulated_wire/${color.name}/$state")
        }.toMap()
      }.toMap()

    val Bakery = WireModel(0.25, 0.1875, 32.0, Textures.values.flatMap { it.values },
      { Textures[it[PropColor]]!![if (it[PropPowered]) "on" else "off"]!! },
      { Textures[EnumDyeColor.byMetadata(it.itemDamage)]!!["on"]!! }
    )

    val PropColor = PropertyEnum.create("color", EnumDyeColor::class.java)
  }
}