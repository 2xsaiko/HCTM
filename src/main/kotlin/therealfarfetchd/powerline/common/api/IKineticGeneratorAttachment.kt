package therealfarfetchd.powerline.common.api

import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

interface IKineticGeneratorAttachment {
  fun isValidPlacement(direction: EnumFacing): Boolean


  fun getPowerLevel(windSpeed: Float)

  fun getWear(windSpeed: Float)

  fun getDurability()

  fun saveData(nbt: QNBTCompound)
  fun loadData(nbt: QNBTCompound)

  fun saveToItem(item: ItemStack)
}