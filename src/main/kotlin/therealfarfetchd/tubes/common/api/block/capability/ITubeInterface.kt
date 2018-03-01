package therealfarfetchd.tubes.common.api.block.capability

import net.minecraft.util.ResourceLocation
import therealfarfetchd.tubes.ModID
import therealfarfetchd.tubes.common.api.item.ColoredItemStack

interface ITubeInterface {
  /**
   * Returns true if the passed stack can be (partially) accepted.
   */
  fun canAcceptItem(c: ColoredItemStack): Boolean

  /**
   * Accept the passed stack. Returned stack are the items that didn't fit.
   */
  fun acceptItem(c: ColoredItemStack): ColoredItemStack

  companion object {
    val DataType = ResourceLocation(ModID, "itemtransport")
  }
}