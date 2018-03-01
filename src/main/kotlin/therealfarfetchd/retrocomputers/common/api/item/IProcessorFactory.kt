package therealfarfetchd.retrocomputers.common.api.item

import net.minecraft.item.ItemStack
import therealfarfetchd.retrocomputers.common.api.cpu.IProcessor

/**
 * Created by marco on 24.06.17.
 */
interface IProcessorFactory<T : IProcessor> {

  operator fun invoke(stack: ItemStack): T

  val processorType: Class<T>

}