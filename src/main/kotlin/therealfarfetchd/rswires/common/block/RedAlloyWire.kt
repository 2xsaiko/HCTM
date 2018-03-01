package therealfarfetchd.rswires.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.model.wire.WireModel
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.api.block.IRedstoneConductor
import therealfarfetchd.rswires.common.api.block.RedstoneWireType
import therealfarfetchd.rswires.common.api.block.TypeInsulated
import therealfarfetchd.rswires.common.api.block.TypeRedAlloy

@BlockDef(registerModels = false, creativeTab = ModID)
class RedAlloyWire : RSBaseWireSingleChannel(0.125, 0.125) {
  override fun filterChannel(otherType: RedstoneWireType, otherChannel: Any?) = true

  override fun checkAdditional(cap: Any?, localCap: Any?): Boolean {
    return if (cap is IRedstoneConductor && localCap is IRedstoneConductor) {
      when (cap.wireType) {
        is TypeRedAlloy,
        is TypeInsulated -> true
        else -> false
      }
    } else false
  }

  override fun getWireType(): RedstoneWireType = TypeRedAlloy

  override fun getItem(): ItemStack = Item.makeStack()

  override val providePowerToGround: Boolean = true
  override val blockType: ResourceLocation = ResourceLocation(ModID, "red_alloy_wire")

  companion object {
    val Block by WrapperImplManager.container(RedAlloyWire::class)
    val Item by WrapperImplManager.item(RedAlloyWire::class)

    val Textures = listOf(
      ResourceLocation(ModID, "blocks/red_alloy_wire/off"),
      ResourceLocation(ModID, "blocks/red_alloy_wire/on")
    )

    val Bakery = WireModel(0.125, 0.125, 32.0,
      Textures,
      { Textures[if (it[PropPowered]) 1 else 0] },
      { Textures[1] }
    )
  }
}