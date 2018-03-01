package therealfarfetchd.retrocomputers.common.api

import therealfarfetchd.quacklib.common.api.util.IView
import java.util.*

/**
 * Created by marco on 28.05.17.
 */
interface IFloppy {
  val uniqueId: UUID
  var label: String
  val sector: IView<Short, List<Byte>?>

  fun trim()

  companion object {
    val EmptyBlock: List<Byte> = List(128, { 0.toByte() })
  }
}