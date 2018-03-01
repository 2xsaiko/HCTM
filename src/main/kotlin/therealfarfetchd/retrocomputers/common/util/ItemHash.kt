package therealfarfetchd.retrocomputers.common.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import therealfarfetchd.retrocomputers.RetroComputers

object ItemHash {

  private var hashes: Map<Int, Item> = emptyMap()

  operator fun invoke(stack: ItemStack): Int {
    if (stack.isEmpty) return 0
    val item = stack.item
    var rl = item.registryName?.toString() ?: return 0
    if (stack.hasSubtypes) rl += "@${stack.itemDamage}"
    val hashCode = rl.hashCode()
    val clashingItem = hashes[hashCode]
    if (clashingItem != null && clashingItem != item) {
      RetroComputers.Logger.fatal("Clashing item hash code! ($clashingItem ${clashingItem.registryName} and $item $rl) Report this to mod author.")
    }
    hashes += hashCode to item
    return hashCode
  }

}