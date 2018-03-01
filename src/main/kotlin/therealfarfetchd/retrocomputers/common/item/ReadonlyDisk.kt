package therealfarfetchd.retrocomputers.common.item

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import therealfarfetchd.quacklib.common.api.util.IView
import therealfarfetchd.retrocomputers.common.api.IFloppy
import therealfarfetchd.retrocomputers.common.api.RCCreativeTab
import therealfarfetchd.retrocomputers.common.api.item.IFloppyProvider
import java.io.InputStream
import java.util.*

/**
 * Created by marco on 24.06.17.
 */
class ReadonlyDisk(vararg val streams: () -> InputStream?) : Item(), IFloppyProvider {

  init {
    maxStackSize = 1
    hasSubtypes = true
    maxDamage = 0
    creativeTab = RCCreativeTab
  }

  override fun showDurabilityBar(stack: ItemStack?): Boolean = false

  override fun getSubItems(tab: CreativeTabs?, items: NonNullList<ItemStack>?) {
    if (tab == creativeTab && items != null) streams.indices.forEach {
      items += ItemStack(this, 1, it)
    }
  }

  override fun getUnlocalizedName(stack: ItemStack?): String = "$unlocalizedName.${stack?.metadata}"

  override fun invoke(stack: ItemStack): IFloppy = Floppy(streams[stack.metadata])

  private class Floppy(val stream: () -> InputStream?) : IFloppy {
    override val uniqueId: UUID = UUID(0, 0)

    override var label: String
      get() = "System Disk"
      set(value) {}

    override fun trim() {}

    override val sector: IView<Short, List<Byte>?> = object : IView<Short, List<Byte>?> {
      override fun get(k: Short): List<Byte>? {
        val stream = stream()
        return stream?.let {
          val skipped = it.skip(k * 128L)
          val ret = if (skipped != k * 128L) null else {
            val data = ByteArray(128)
            if (stream.read(data) != 128) null else data.toList()
          }
          it.close()
          ret
        }
      }

      override fun set(k: Short, v: List<Byte>?) {}
    }
  }

}