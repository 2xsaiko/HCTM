package therealfarfetchd.retrocomputers.common

import java.nio.charset.Charset
import java.util.*

/**
 * Created by marco on 01.07.17.
 */
internal object SuperCompressor { // just kidding it is actually pretty inefficient

  val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-*/!§$%&(){}[]=?`'<>,.-;:_"
  val sep = '|'

  //  fun compress(str: String, encw: Int = 4, repeat: Boolean = true): String {
  //    //      val str = Base64.getEncoder().encodeToString(str.toByteArray(Charset.forName("UTF-8")))
  //    val sb = StringBuilder()
  //    sb += encw.toIChar()
  //    sb += repeat.toIChar()
  //    var patternSet = emptySet<String>()
  //    for (i in 0 until (str.length ceil_div encw)) {
  //      patternSet += str.substring(i * encw until minOf((i + 1) * encw, str.length))
  //    }
  //    val patterns = patternSet.toList()
  //
  //    for (p in patterns) sb += p
  //    sb += sep
  //
  //    if (repeat) {
  //      var latestPat = ""
  //      var patcount = 0
  //      for (i in 0 until (str.length ceil_div encw)) {
  //        val part = str.substring(i * encw until minOf((i + 1) * encw, str.length))
  //        if (part == latestPat) patcount++
  //        else {
  //          if (latestPat != "") {
  //            sb += patcount.toIChar()
  //            sb += patterns.indexOf(latestPat).toIChar()
  //          }
  //          latestPat = part
  //          patcount = 0
  //        }
  //      }
  //      if (latestPat != "") {
  //        sb += patcount.toIChar()
  //        sb += patterns.indexOf(latestPat).toIChar()
  //      }
  //    } else {
  //      for (i in 0 until (str.length ceil_div encw)) {
  //        val part = str.substring(i * encw until minOf((i + 1) * encw, str.length))
  //        sb += patterns.indexOf(part).toIChar()
  //      }
  //    }
  //
  //    return sb.toString()
  //  }

  fun decompress(str: String): String {
    val (header, data) = str.split(sep)
    val encw = header[0].toIInt()
    val repeat = header[1].toIBool()
    val comprdef = header.substring(2 until header.length)
    var dict = emptyList<String>()
    for (i in 0 until comprdef.length / encw) {
      dict += comprdef.substring(i * encw until (i + 1) * encw)
    }
    if (repeat) {
      var flag = false
      var count = 0
      var out = ""
      for (char in data) {
        if (!flag) {
          count = char.toIInt()
        } else {
          val pat = dict[char.toIInt()]
          for (i in 0..count) {
            out += pat
          }
        }
        flag = !flag
      }
      return String(Base64.getDecoder().decode(out), Charset.forName("UTF-8"))
    } else {
      return String(Base64.getDecoder().decode(data.map { dict[it.toIInt()] }.reduce { acc, s -> acc + s }), Charset.forName("UTF-8"))
    }
  }

  private infix fun Int.ceil_div(other: Int) = this / other + if (this % other != 0) 1 else 0

  private fun Int.toIChar() = chars[this]
  private fun Boolean.toIChar() = chars[if (this) 1 else 0]

  private fun Char.toIInt() = chars.indexOf(this)
      .apply { if (this == -1) throw IllegalArgumentException("${this@toIInt} is not a valid char to convert to int!") }

  private fun Char.toIBool() = chars.indexOf(this)
      .apply { if (this !in 0..1) throw IllegalArgumentException("${this@toIBool} is not a valid char to convert to bool!") } == 1

  private operator fun StringBuilder.plusAssign(any: Any?) {
    this.append(any)
  }

}