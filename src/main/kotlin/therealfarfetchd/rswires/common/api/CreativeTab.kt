package therealfarfetchd.rswires.common.api

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.util.AutoLoad
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.block.RedAlloyWire

@AutoLoad
object CreativeTab : CreativeTabs(ModID) {
  override fun getTabIconItem(): ItemStack = RedAlloyWire.Item.makeStack()
}