package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.util.ItemDef
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.component.ComponentCPU

@ItemDef(registerModels = true, creativeTab = ModID)
object ItemCPU : ItemDevice() {
  init {
    registryName = ResourceLocation(ModID, "cpu2")
  }

  override fun create(stack: ItemStack) = ComponentCPU()
}