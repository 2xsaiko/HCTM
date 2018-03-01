package therealfarfetchd.tubes.common.api

import net.minecraft.creativetab.CreativeTabs
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.util.AutoLoad
import therealfarfetchd.tubes.ModID
import therealfarfetchd.tubes.common.block.Tube

@AutoLoad
object CreativeTab : CreativeTabs(ModID) {
  override fun getTabIconItem() = Tube.Item.makeStack()
}