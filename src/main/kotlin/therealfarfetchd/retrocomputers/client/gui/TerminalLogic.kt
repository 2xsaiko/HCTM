package therealfarfetchd.retrocomputers.client.gui

import org.lwjgl.input.Keyboard
import therealfarfetchd.quacklib.client.api.gui.AbstractGuiLogic
import therealfarfetchd.quacklib.common.api.extensions.copyTo
import therealfarfetchd.quacklib.common.api.extensions.pmod
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.client.gui.elements.Screen
import therealfarfetchd.retrocomputers.common.block.Terminal
import therealfarfetchd.retrocomputers.common.net.PacketTerminalAction
import kotlin.experimental.xor

class TerminalLogic : AbstractGuiLogic() {
  val screen: Screen by component()

  val qb: Terminal by params()

  override fun init() {
    root.key { char, keyCode ->
      val result: Byte? =
        when (char) {
          in '\u0001'..'\u007F' -> char.toByte()
          else -> null
        } ?:
        when (keyCode) {
          Keyboard.KEY_HOME -> 0x80
          Keyboard.KEY_END -> 0x81
          Keyboard.KEY_UP -> 0x82
          Keyboard.KEY_DOWN -> 0x83
          Keyboard.KEY_LEFT -> 0x84
          Keyboard.KEY_RIGHT -> 0x85
          else -> null
        }?.toByte()

      if (result != null) {
        val packet = PacketTerminalAction(qb.world.provider.dimension, qb.pos, result)
        RetroComputers.Net.sendToServer(packet)
      }
    }
  }

  override fun update() {
    qb.charset.copyTo(screen.charset)
    qb.screendata.copyTo(screen.chars)

    val curp = qb.curX + qb.curY * Terminal.ScreenWidth
    when (qb.curMode) {
      1 -> {
        screen.chars[curp] = screen.chars[curp] xor -0x80
      }
      2 -> {
        if (qb.world.totalWorldTime pmod 10 < 5)
          screen.chars[curp] = screen.chars[curp] xor -0x80
      }
    }
  }
}