package therealfarfetchd.retrocomputers.common.item

import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import therealfarfetchd.quacklib.common.api.util.IView
import therealfarfetchd.quacklib.common.api.util.ItemDef
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.api.IFloppy
import therealfarfetchd.retrocomputers.common.api.item.IFloppyProvider
import java.io.File
import java.io.RandomAccessFile
import java.util.*
import kotlin.experimental.or

/**
 * Created by marco on 24.06.17.
 */
@ItemDef(creativeTab = ModID)
object WritableDisk : Item(), IFloppyProvider {

  init {
    maxStackSize = 1
    registryName = ResourceLocation("retrocomputers", "writable_disk")
  }

  override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag?) {
    val nbt = stack.tagCompound
    if (nbt != null) {
      if (nbt.hasUniqueId("Serial")) tooltip.add("Serial No.: ${nbt.getUniqueId("Serial")}")
      if (nbt.hasKey("CustomPath")) tooltip.add("Custom path: ${nbt.getString("CustomPath")}")
    }
    super.addInformation(stack, worldIn, tooltip, flagIn)
  }

  override fun getItemStackDisplayName(stack: ItemStack): String {
    val nbt = stack.tagCompound
    return if (nbt != null && nbt.hasKey("Label")) nbt.getString("Label")
    else super.getItemStackDisplayName(stack)
  }

  override fun invoke(stack: ItemStack): IFloppy {
    val nbt = stack.tagCompound ?: NBTTagCompound().also { stack.tagCompound = it }
    if (!nbt.hasUniqueId("Serial")) nbt.setUniqueId("Serial", UUID.randomUUID())
    var path = nbt.getUniqueId("Serial").toString()
    if (nbt.hasKey("CustomPath")) path = nbt.getString("CustomPath")
    return Floppy(File(DimensionManager.getCurrentSaveRootDirectory(), "rcdisks/$path"), stack)
  }

  class Floppy(private val file: File, private val stack: ItemStack) : IFloppy {
    private val nbt = stack.tagCompound!!

    override val uniqueId: UUID
      get() = nbt.getUniqueId("Serial")!!

    override var label: String
      get() = if (nbt.hasKey("Label")) nbt.getString("Label") else "Floppy Disk"
      set(value) {
        nbt.setString("Label", value)
      }

    override val sector: IView<Short, List<Byte>?> = object : IView<Short, List<Byte>?> {
      override fun get(k: Short): List<Byte>? {
        check(file.parentFile.exists() || file.parentFile.mkdirs())
        return if (file.exists()) {
          val istr = file.inputStream()
          if (istr.skip(k * 128L) == k * 128L) {
            val b = ByteArray(128)
            if (istr.read(b) != 128) null else b.toList()
          } else null
        } else null
      }

      override fun set(k: Short, v: List<Byte>?) {
        check(file.parentFile.exists() || file.parentFile.mkdirs())
        val raf = RandomAccessFile(file, "rw")
        raf.seek(k * 128L)
        raf.write((v ?: IFloppy.EmptyBlock).toByteArray())
        raf.close()
        trim()
      }
    }

    override fun trim() {
      val raf = RandomAccessFile(file, "rw")
      if (raf.length() != 0L) {
        val max = (raf.length() / 128).toInt()
        val zeroSectors = BooleanArray(max)
        for (i in 0 until max) {
          raf.seek(i * 128L)
          var b = 0.toByte()
          for (j in 0..127) b = b or raf.readByte()
          zeroSectors[i] = b == 0.toByte()
        }
        val lastFull = zeroSectors
            .mapIndexed { index: Int, b: Boolean -> index to b }
            .filterNot { it.second }
            .map { it.first }
            .max()
        raf.channel.truncate(((lastFull ?: 0) + 1) * 128L)
      } else raf.channel.truncate(0L)
      raf.close()
    }

  }
}