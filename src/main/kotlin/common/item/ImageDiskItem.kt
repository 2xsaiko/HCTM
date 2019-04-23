package therealfarfetchd.retrocomputers.common.item

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import therealfarfetchd.retrocomputers.common.item.ext.ItemDisk

class ImageDiskItem(image: Identifier): Item(Item.Settings().stackSize(1)), ItemDisk {

  private val path = Identifier(image.namespace, "disks/${image.path}.img")

  override fun getLabel(stack: ItemStack): String = "System Disk"

}