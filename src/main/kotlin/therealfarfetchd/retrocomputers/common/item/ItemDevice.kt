package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import therealfarfetchd.retrocomputers.common.api.component.Component

abstract class ItemDevice : Item() {
  abstract fun create(stack: ItemStack): Component
}