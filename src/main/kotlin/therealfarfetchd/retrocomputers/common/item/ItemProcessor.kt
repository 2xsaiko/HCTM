package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.util.ItemDef
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.api.item.IProcessorFactory
import therealfarfetchd.retrocomputers.common.cpu.Processor

@ItemDef(creativeTab = ModID)
object ItemProcessor : Item(), IProcessorFactory<Processor> {
  init {
    registryName = ResourceLocation(ModID, "cpu")
  }

  override fun invoke(stack: ItemStack): Processor = Processor()

  override val processorType: Class<Processor> = Processor::class.java
}