package therealfarfetchd.retrocomputers.common.item.ext

import net.minecraft.item.ItemStack

interface ItemDisk {

  fun getLabel(stack: ItemStack): String

}