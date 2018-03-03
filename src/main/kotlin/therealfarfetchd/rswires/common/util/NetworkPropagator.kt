package therealfarfetchd.rswires.common.util

import therealfarfetchd.quacklib.common.api.extensions.mapWithCopy
import therealfarfetchd.quacklib.common.api.util.EnumFacingExtended
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
      var skipIter = false
      try {
        network.forEach { it.propagating = true }
        val channelHasPower = network.any { it.getInput() }
        network.forEach { it.setOutput(channelHasPower) }
        network.forEach { it.propagating = false }
        skipIter = true
        network.forEach { it.endPropagating() }
      } finally {
        if (!skipIter) network.forEach { it.propagating = false }
        pending -= network
      }
    }
  }

  private class Context {
    var network: Set<K> = emptySet()

    fun mapNetwork(cond: K) {
      if (cond in network) return
      network += cond
      EnumFacingExtended.Values
        .mapNotNull(cond.first.ns::getNeighborAt)
        .filter { it.allowChannel(cond.first.wireType, cond.second) }
        .mapWithCopy { it.mapChannel(cond.first.wireType, cond.second) }
        .toSet()
        .forEach(this::mapNetwork)
    }
  }
}