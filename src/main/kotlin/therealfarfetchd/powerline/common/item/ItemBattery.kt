package therealfarfetchd.powerline.common.item

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.item.IChargeItem
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.util.ItemDef

@ItemDef(creativeTab = ModID)
object ItemBattery : Item(), IChargeItem {
  init {
    registryName = ResourceLocation(ModID, "battery")

    maxDamage = 200
    maxStackSize = 1
    canRepair = false
    hasSubtypes = true
  }

  override val capacity: Int = 200

  override fun getCharge(stack: ItemStack): Int {
    return maxOf(0, minOf(stack.metadata, capacity))
  }

  override fun canApplyAtEnchantingTable(stack: ItemStack?, enchantment: Enchantment?): Boolean = false

  override fun setCharge(stack: ItemStack, amount: Int): ItemStack {
    return if (stack.count > 1) stack.copy()
    else stack.item.makeStack(meta = maxOf(0, minOf(amount, capacity)))
  }

  override fun getDurabilityForDisplay(stack: ItemStack): Double = 1.0 - (getCharge(stack).toDouble() / capacity)

  override fun showDurabilityBar(stack: ItemStack): Boolean = getDurabilityForDisplay(stack) != 1.0

  override fun getItemStackLimit(stack: ItemStack): Int = when (getCharge(stack)) {
    0 -> 64
    else -> 1
  }

  override fun getSubItems(tab: CreativeTabs, items: NonNullList<ItemStack>) {
    if (this.isInCreativeTab(tab)) {
      items.add(makeStack(meta = 0))
      items.add(makeStack(meta = capacity))
    }
  }
}
