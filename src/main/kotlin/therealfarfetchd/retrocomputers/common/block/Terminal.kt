package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.material.Material
import net.minecraft.item.ItemStack
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.extensions.*
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal
import therealfarfetchd.retrocomputers.common.util.mapper
import kotlin.experimental.xor

/**
 * Created by marco on 25.06.17.
 */
@BlockDef(creativeTab = ModID)
class Terminal : Horizontal(1), ITickable {
  /*
   * 0: current screen row
   * 1: cursor x
   * 2: cursor y
   * 3: cursor mode (0: hidden, 1: solid, 2: blink)
   * 4: key buffer start (16 byte buffer)
   * 5: key buffer position
   * 6: key value at buffer start
   * 7: command (1: fill, 2: invert; 3: shift; 4: reset charset)
   * 8: blit x start / fill value
   * 9: blit y start
   * A: blit x offset
   * B: blit y offset
   * C: blit width
   * D: blit height
   * E: editable char
   * F: - unused -
   * 10 - 60: Screen line
   * 61 - 68: Char bitmap data
   * 69 - FF: - unused -
  */

  val charset: ByteArray = DefaultCharset.clone()
  val screendata: ByteArray = ByteArray(ScreenWidth * ScreenHeight, { 0x20 })
  var row by mapper(0, { it pmod ScreenHeight }, { it }); private set
  var curX by mapper(0, { it pmod ScreenWidth }, { it }); private set
  var curY by mapper(0, { it pmod ScreenHeight }, { it }); private set
  var curMode by mapper(2, { it pmod 3 }, { it }); private set
  var blitMode by mapper(0, { it pmod 5 }, { it }); private set
  var bOriginX by mapper(0, { it pmod ScreenWidth }, { it }); private set
  var bOriginY by mapper(0, { it pmod ScreenHeight }, { it }); private set
  var bOffsetX by mapper(0, { it pmod ScreenWidth }, { it }); private set
  var bOffsetY by mapper(0, { it pmod ScreenHeight }, { it }); private set
  var bWidth: Int = 0; private set
  var bHeight: Int = 0; private set
  var charEdit: Byte = 0; private set

  val kbuf: ByteArray = ByteArray(16)
  var kbHead by mapper(0, { it pmod kbuf.size }, { it })
  var kbTail by mapper(0, { it pmod kbuf.size }, { it })

  private val clientCL = ChangeListener(this::charset, this::screendata, this::curX, this::curY, this::curMode)
  private val worldCL = ChangeListener(this::charset, this::screendata, this::curX, this::curY, this::curMode,
    this::blitMode, this::bOriginX, this::bOriginY, this::bOffsetX, this::bOffsetY, this::bWidth, this::bHeight,
    this::kbuf, this::kbHead, this::kbTail)

  override fun update() {
    if (world.isServer) {
      if (blitMode != 0) {
        var width = minOf(bWidth, ScreenWidth - bOffsetX)
        var height = minOf(bHeight, ScreenHeight - bOffsetY)
        val srcStart = bOriginX + ScreenWidth * bOriginY
        val destStart = bOffsetX + ScreenWidth * bOffsetY
        when (blitMode) {
          1 -> {
            // fill
            for (i in 0 until width)
              (0 until height)
                .asSequence()
                .forEach { j -> screendata[destStart + i + ScreenWidth * j] = bOriginX.toByte() }
          }
          2 -> {
            // invert
            for (i in 0 until width)
              (0 until height)
                .asSequence()
                .map { destStart + i + ScreenWidth * it }
                .forEach { screendata[it] = screendata[it] xor 0x80.toByte() }
          }
          3 -> {
            // shift
            width = minOf(width, ScreenWidth - bOriginX)
            height = minOf(height, ScreenHeight - bOriginY)
            for (i in 0 until width)
              (0 until height)
                .asSequence()
                .map { i + ScreenWidth * it }
                .forEach { screendata[destStart + it] = screendata[srcStart + it] }
          }
          4 -> DefaultCharset.copyTo(charset)
        }
        blitMode = 0
      }
      if (worldCL.valuesChanged()) dataChanged()
      if (clientCL.valuesChanged()) clientDataChanged()
    }
  }

  // @Optional.Method(modid = "mirage")
  // override fun getColoredLight(): Light? {
  //   return Light.builder()
  //     .pos(pos)
  //     .radius(5f)
  //     .cone(Vec3d(facing.directionVec), 0.75F)
  //     .color(0.78f, 0.57f, 0.01f)
  //     .build()
  // }

  override fun peek(addr: Byte): Byte {
    val uaddr = addr.unsigned
    return when (uaddr) {
      0 -> row.toByte()
      1 -> curX.toByte()
      2 -> curY.toByte()
      3 -> curMode.toByte()
      4 -> kbTail.toByte()
      5 -> kbHead.toByte()
      6 -> kbuf[kbTail]
      7 -> blitMode.toByte()
      8 -> bOriginX.toByte()
      9 -> bOriginY.toByte()
      10 -> bOffsetX.toByte()
      11 -> bOffsetY.toByte()
      12 -> bWidth.toByte()
      13 -> bHeight.toByte()
      14 -> charEdit
      in 0x10..0x60 -> screendata[row * ScreenWidth + uaddr - 0x10]
      in 0x61..0x68 -> charset[8 * charEdit.unsigned + uaddr - 0x61]
      else -> 0
    }
  }

  override fun poke(addr: Byte, b: Byte) {
    val uaddr = addr.unsigned
    val ub = b.unsigned
    when (uaddr) {
      0 -> row = ub
      1 -> curX = ub
      2 -> curY = ub
      3 -> curMode = ub
      4 -> kbTail = b.unsigned
      5 -> kbHead = b.unsigned
      6 -> kbuf[kbTail] = b
      7 -> blitMode = ub
      8 -> bOriginX = ub
      9 -> bOriginY = ub
      10 -> bOffsetX = ub
      11 -> bOffsetY = ub
      12 -> bWidth = ub
      13 -> bHeight = ub
      14 -> charEdit = b
      in 0x10..0x60 -> screendata[row * ScreenWidth + uaddr - 0x10] = b
      in 0x61..0x68 -> charset[8 * charEdit.unsigned + uaddr - 0x61] = b
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.bytes["cs"] = charset
    nbt.bytes["ch"] = screendata
    nbt.ubyte["cx"] = curX
    nbt.ubyte["cy"] = curY
    nbt.ubyte["cm"] = curMode
    if (target == DataTarget.Save) {
      nbt.bytes["Kb"] = kbuf
      nbt.ubyte["KbH"] = kbHead
      nbt.ubyte["KbT"] = kbTail
      nbt.ubyte["Row"] = row
      nbt.ubyte["BM"] = blitMode
      nbt.ubyte["BOrX"] = bOriginX
      nbt.ubyte["BOrY"] = bOriginY
      nbt.ubyte["BOfX"] = bOffsetX
      nbt.ubyte["BOfY"] = bOffsetY
      nbt.ubyte["BW"] = bWidth
      nbt.ubyte["BH"] = bHeight
      nbt.byte["CE"] = charEdit
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    nbt.bytes["cs"].copyTo(charset)
    nbt.bytes["ch"].copyTo(screendata)
    curX = nbt.ubyte["cx"]
    curY = nbt.ubyte["cy"]
    curMode = nbt.ubyte["cm"]
    if (target == DataTarget.Save) {
      kbuf.copyTo(nbt.bytes["Kb"])
      kbHead = nbt.ubyte["KbH"]
      kbTail = nbt.ubyte["KbT"]
      row = nbt.ubyte["Row"]
      blitMode = nbt.ubyte["BM"]
      bOriginX = nbt.ubyte["BOrX"]
      bOriginY = nbt.ubyte["BOrY"]
      bOffsetX = nbt.ubyte["BOfX"]
      bOffsetY = nbt.ubyte["BOfY"]
      bWidth = nbt.ubyte["BW"]
      bHeight = nbt.ubyte["BH"]
      charEdit = nbt.byte["CE"]
    }
  }

  override val material: Material = Material.IRON

  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "terminal")

  companion object {
    const val ScreenWidth = 80
    const val ScreenHeight = 50

    val DefaultCharset by lazy {
      val str = RetroComputers::class.java.classLoader.getResourceAsStream("assets/$ModID/charset.bin")
      checkNotNull(str)
      val cs = ByteArray(2048)
      str.read(cs)
      str.close()
      cs
    }

    val Block by WrapperImplManager.container(Terminal::class)
    val Item by WrapperImplManager.item(Terminal::class)
  }
}