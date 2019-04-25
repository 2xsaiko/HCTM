package therealfarfetchd.retrocomputers.common.item.ext

import net.minecraft.item.ItemStack

interface ItemDisk {

  fun getLabel(stack: ItemStack): String

  fun setLabel(stack: ItemStack, str: String)

  fun sector(index: Int): Sector?

  interface Sector : AutoCloseable {
    val data: ByteArray
  }

}