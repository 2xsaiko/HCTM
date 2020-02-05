package therealfarfetchd.hctm.common.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <T : Any> delegatedNotNull(crossinline getter: () -> T?) = object : ReadOnlyProperty<Any?, T> {
  override fun getValue(thisRef: Any?, property: KProperty<*>): T {
    return getter() ?: error("Can't retrieve value from ${property.name}; not initialized!")
  }
}

fun <K, R, T> Map<K, ReadOnlyProperty<R, T>>.flatten(): ReadOnlyProperty<R, Map<K, T>> {
  return object : ReadOnlyProperty<R, Map<K, T>> {
    override fun getValue(thisRef: R, property: KProperty<*>): Map<K, T> {
      return this@flatten.mapValues { (_, it) -> it.getValue(thisRef, property) }
    }
  }
}