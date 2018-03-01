package therealfarfetchd.retrocomputers.common.api

/**
 * Created by marco on 27.05.17.
 */
object Conversions {

  /**
   * I don't even know why I need this, but the normal modulo somehow returns negative values
   */
  infix fun Int.mod_p(other: Int): Int {
    var mod = this % other
    if (mod < 0) mod += other
    return mod
  }

  val Int.boolean get() = this != 0
  val Boolean.int get() = if (this) -1 else 0

  val Byte.unsigned: Int get() = this.toInt() and 0xFF
  val Short.unsigned: Int get() = this.toInt() and 0xFFFF

}