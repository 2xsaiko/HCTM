package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.util.ItemDef
import therealfarfetchd.retrocomputers.ModID

/**
 * Created by marco on 24.06.17.
 */
@ItemDef(creativeTab = ModID)
object ItemDebug : Item() {

  init {
    registryName = ResourceLocation(ModID, "debug")
  }

  override fun getItemStackDisplayName(stack: ItemStack): String {
    val s = (Math.random() * 14).toInt().toString(16)
    val s1 = if (Math.random() < 0.1) "§k" else ""
    return "§$s$s1${super.getItemStackDisplayName(stack)}"
  }

}