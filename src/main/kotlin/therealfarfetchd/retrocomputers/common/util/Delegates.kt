package therealfarfetchd.retrocomputers.common.util

import kotlin.reflect.KProperty

/**
 * Created by marco on 25.06.17.
 */

class mapper<in R, I, S>(init: I, val op: (I) -> S, val op2: (S) -> I) {
  var value: S = op(init)

  operator fun getValue(thisRef: R, property: KProperty<*>): I = op2(value)

  operator fun setValue(thisRef: R, property: KProperty<*>, value: I) {
    this.value = op(value)
  }

}