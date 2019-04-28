package therealfarfetchd.retrocomputers.common.item.ext

import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld

interface ItemDisk {

  fun getLabel(stack: ItemStack): String

  fun setLabel(stack: ItemStack, str: String)

  fun sector(stack: ItemStack, world: ServerWorld, index: Int): Sector?

  interface Sector : AutoCloseable {
    val data: ByteArray

    fun isEmpty(): Boolean
  }

}