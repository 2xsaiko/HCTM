package therealfarfetchd.powerline.common.api.item

import net.minecraft.item.ItemStack

interface IChargeItem {
  val capacity: Int

  fun getCharge(stack: ItemStack): Int

  fun setCharge(stack: ItemStack, amount: Int): ItemStack
}