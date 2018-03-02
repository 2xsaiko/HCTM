package therealfarfetchd.rswires.common.util

import therealfarfetchd.quacklib.common.api.extensions.mapWithCopy
import therealfarfetchd.quacklib.common.api.util.EnumFaceLocation
import therealfarfetchd.quacklib.common.api.util.Profiler
import therealfarfetchd.rswires.common.api.block.IRedstoneConductor

typealias K = Pair<IRedstoneConductor, Any?>

fun K.getInput() = first.getInput(second)

var K.propagating
  get() = first.isPropagating(second)
  set(value) {
    first.setPropagating(second, value)
  }

fun K.setOutput(b: Boolean) = first.setOutput(second, b)

fun K.endPropagating() = first.endPropagating(second)

object NetworkPropagator {
  private var pending: Set<K> = emptySet()

  fun schedulePropagate(condIn: IRedstoneConductor, channel: Any?) {
    pending += condIn to channel
  }

  internal fun tickPropagation() {
    Profiler("propagation", 100) {
      while (pending.isNotEmpty()) {
        val (cond, ch) = pending.first()
        propagate(cond, ch)
      }
    }
  }

  private fun propagate(condIn: IRedstoneConductor, channel: Any?) {
    with(Context()) {
      mapNetwork(condIn to channel)
      try {
        network.onEach { it.propagating = true }
          .forEach { it.setOutput(false) }

        val enabled = network.filter { it.getInput() }
        if (enabled.isNotEmpty()) {
          network.forEach { it.setOutput(true) }
        }
        network.forEach { it.propagating = false }
        network.forEach { it.endPropagating() }
      } finally {
        network.forEach { it.propagating = false }
        pending -= network
      }
    }
  }

  private class Context {
    var network: Set<K> = emptySet()

    fun mapNetwork(cond: K) {
      if (cond in network) return
      network += cond
      EnumFaceLocation.Values
        .mapNotNull(cond.first.ns::getNeighborAt)
        .filter { it.allowChannel(cond.first.wireType, cond.second) }
        .mapWithCopy { it.mapChannel(cond.first.wireType, cond.second) }
        .toSet()
        .forEach(this::mapNetwork)
    }
  }
}