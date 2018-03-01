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
class LVPowerline : PowerlineBase(0.25, 0.1875, DefaultConductorConfiguration.LVCable) {
  override fun getItem(): ItemStack = Item.makeStack()
  override val wireType: PowerType = PowerType.LowVoltage
  override val dataType: ResourceLocation = IPowerConnectable.TypeLV

  override val blockType: ResourceLocation = ResourceLocation(ModID, "lvwire")

  companion object {
    val Block by WrapperImplManager.container(LVPowerline::class)
    val Item by WrapperImplManager.item(LVPowerline::class)

    val Bakery = WireModel(0.25, 0.1875, 32.0, ResourceLocation(ModID, "blocks/lvwire"))
  }
}