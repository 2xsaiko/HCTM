package therealfarfetchd.powerline.common.api

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.HVPowerline
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.util.AutoLoad

@AutoLoad
object PowerlineCreativeTab : CreativeTabs(ModID) {
  override fun getTabIconItem(): ItemStack = HVPowerline.Item.makeStack()
}