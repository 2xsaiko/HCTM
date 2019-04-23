package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import therealfarfetchd.retrocomputers.common.item.ext.ItemDisk

class UserDiskItem : Item(Item.Settings().stackSize(1)), ItemDisk {

  override fun getLabel(stack: ItemStack): String {
    TODO("not implemented")
  }

}