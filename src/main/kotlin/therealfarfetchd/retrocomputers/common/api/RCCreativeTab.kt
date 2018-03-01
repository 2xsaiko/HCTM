package therealfarfetchd.retrocomputers.common.api

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.util.AutoLoad
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.Terminal

/**
 * Created by marco on 28.05.17.
 */
@AutoLoad
object RCCreativeTab : CreativeTabs(ModID) {
  override fun getTabIconItem(): ItemStack = Terminal.Item.makeStack()
}