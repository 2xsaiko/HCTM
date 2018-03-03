package therealfarfetchd.retrocomputers.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.model.wire.WireModel
import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.wires.BlockWire
import therealfarfetchd.quacklib.common.api.wires.getNeighbor
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.api.block.BusContainer
import therealfarfetchd.retrocomputers.common.api.block.capability.IBusConnectable

@BlockDef(registerModels = false, creativeTab = ModID)
class RibbonCable : BlockWire<BusContainer>(0.5, 0.0625) {

  override val dataType: ResourceLocation = IBusConnectable.DataType
  override val data: BusContainer = BusContainer(INeighborSupport { getNeighbor(it) as? BusContainer })

  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "ribbon_cable")

  companion object {
    val Block by WrapperImplManager.container(RibbonCable::class)
    val Item by WrapperImplManager.item(RibbonCable::class)

    val Bakery = WireModel(0.5, 0.0625, 32.0, ResourceLocation(ModID, "blocks/ribbon_cable"))
  }
}