package therealfarfetchd.retrocomputers.common.api.item

import net.minecraft.item.ItemStack
import therealfarfetchd.retrocomputers.common.api.IFloppy

/**
 * Created by marco on 24.06.17.
 */
interface IFloppyProvider {
  operator fun invoke(stack: ItemStack): IFloppy
}