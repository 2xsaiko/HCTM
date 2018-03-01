package therealfarfetchd.tubes.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.model.wire.CenteredWireModel
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.clientOnly
import therealfarfetchd.tubes.ModID

//@BlockDef(registerModels = false, creativeTab = ModID)
class RedstoneTube : Tube() {
  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "rs_tube")

  companion object {
    val Item by WrapperImplManager.item(RedstoneTube::class)
    val Block by WrapperImplManager.container(RedstoneTube::class)

    val Bakery by clientOnly { CenteredWireModel(ResourceLocation(ModID, "blocks/rs_tube_off"), 16.0f, 0.5) }
  }
}