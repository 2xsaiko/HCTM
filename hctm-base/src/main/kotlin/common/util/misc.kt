package net.dblsaiko.hctm.common.util

inline fun <reified T> Any?.ifIsType(elseValue: Boolean = false, op: (T) -> Boolean): Boolean =
  if (this is T) op(this) else elseValue