package therealfarfetchd.powerline.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.DefaultConductorConfiguration
import therealfarfetchd.powerline.common.api.PowerType
import therealfarfetchd.powerline.common.api.block.capability.IPowerConnectable
import therealfarfetchd.quacklib.client.api.model.wire.WireModel
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef

@BlockDef(registerModels = false, creativeTab = ModID)
class HVPowerline : PowerlineBase(0.375, 0.25, DefaultConductorConfiguration.HVCable) {
  override fun getItem(): ItemStack = Item.makeStack()
  override val wireType: PowerType = PowerType.HighVoltage
  override val dataType: ResourceLocation = IPowerConnectable.TypeHV

  override val blockType: ResourceLocation = ResourceLocation(ModID, "hvwire")

  companion object {
    val Block by WrapperImplManager.container(HVPowerline::class)
    val Item by WrapperImplManager.item(HVPowerline::class)

    val Bakery = WireModel(0.375, 0.25, 32.0, ResourceLocation(ModID, "blocks/hvwire"))
  }
}