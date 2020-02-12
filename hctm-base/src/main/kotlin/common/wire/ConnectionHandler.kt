package net.dblsaiko.hctm.common.wire

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

/**
 * Enumerates all possible connections. Returns a set of sequences where each
 * sequence is seperately processed until the first matching connection in each
 * sequence has been found.
 */
interface ConnectionDiscoverer {
  fun getPossibleConnections(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<Sequence<NetNode>>
}

/**
 * Used to filter possible connections, for example to ensure that only
 * redstone wires connect to redstone wires, or ribbon cables only connect
 * to themselves and peripherals, ...
 */
interface ConnectionFilter {
  fun accepts(self: NetNode, other: NetNode): Boolean

  infix fun and(that: ConnectionFilter): ConnectionFilter {
    return object : ConnectionFilter {
      override fun accepts(self: NetNode, other: NetNode): Boolean {
        return this@ConnectionFilter.accepts(self, other) && that.accepts(self, other)
      }
    }
  }

  companion object {
    inline operator fun invoke(crossinline op: (self: NetNode, other: NetNode) -> Boolean) = object : ConnectionFilter {
      override fun accepts(self: NetNode, other: NetNode): Boolean {
        return op(self, other)
      }
    }

    inline fun <reified T> forClass() = object : ConnectionFilter {
      override fun accepts(self: NetNode, other: NetNode): Boolean {
        return self.data.ext is T && other.data.ext is T
      }
    }
  }
}

fun find(cd: ConnectionDiscoverer, f: ConnectionFilter?, node: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
  return cd.getPossibleConnections(node, world, pos, nv)
    .mapNotNull { it.firstOrNull { f?.accepts(node, it) ?: true } }
    .toSet()
}

/**
 * Create a connection handler from DSL. Provides tryConnect method for use in PartExt
 */
fun <E : PartExt, T> connectionDiscoverer(op: ConnectionDiscovererScope<E, T>.() -> Unit): ConnectionDiscoverer {
  val sc = ConnectionDiscovererScopeImpl<E, T>().also(op)
  return ConnectionDiscovererImpl(sc.parts)
}

private class ConnectionDiscovererImpl<E : PartExt, T>(private val parts: List<ConnectionDiscovererPart<E, T>>) : ConnectionDiscoverer {

  override fun getPossibleConnections(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<Sequence<NetNode>> {
    self as TNetNode<E> // yep, this is unsafe, but if you pass the wrong type of NetNode here you're a dumbass
    val map = mutableMapOf<T, MutableList<ConnectionDiscovererPart<E, T>>>()
    for (part in parts) {
      for (output in part.getPotentialOutputs(self, world, pos)) {
        map.computeIfAbsent(output, { mutableListOf() }) += part
      }
    }
    return map.keys.map { output ->
      sequence {
        for (part in map.getValue(output)) {
          part.tryConnect(output, self, world, pos, nv).forEach { yield(it) }
        }
      }
    }.toSet()
  }

}

private abstract class ConnectionDiscovererPart<E : PartExt, T> {
  abstract fun getPotentialOutputs(self: TNetNode<E>, world: ServerWorld, pos: BlockPos): List<T>

  abstract fun tryConnect(output: T, self: TNetNode<E>, world: ServerWorld, pos: BlockPos, nv: NodeView): List<NetNode>
}

interface ConnectionDiscovererScope<E : PartExt, T> {
  fun connectionRule(op: PartConfigScope<E, T>.() -> Unit)
}

interface PartConfigScope<E : PartExt, T> {
  fun forOutputs(op: OutputsScope<E>.() -> List<T>)
  fun connect(op: ConnectScope<E, T>.() -> List<NetNode>)
}

interface OutputsScope<E : PartExt> {
  val self: TNetNode<E>
  val world: ServerWorld
  val pos: BlockPos
}

interface ConnectScope<E : PartExt, out T> {
  val output: T
  val self: TNetNode<E>
  val world: ServerWorld
  val pos: BlockPos
  val nv: NodeView
}

private class ConnectionDiscovererScopeImpl<E : PartExt, T> : ConnectionDiscovererScope<E, T> {
  val parts = mutableListOf<ConnectionDiscovererPart<E, T>>()

  override fun connectionRule(op: PartConfigScope<E, T>.() -> Unit) {
    val pcs = PartConfigScopeImpl<E, T>().also(op)
    parts += object : ConnectionDiscovererPart<E, T>() {
      override fun getPotentialOutputs(self: TNetNode<E>, world: ServerWorld, pos: BlockPos): List<T> =
        pcs.forOutputs(OutputsScopeImpl(self, world, pos))

      override fun tryConnect(output: T, self: TNetNode<E>, world: ServerWorld, pos: BlockPos, nv: NodeView): List<NetNode> =
        pcs.connect(ConnectScopeImpl(output, self, world, pos, nv))
    }
  }
}

private class PartConfigScopeImpl<E : PartExt, T> : PartConfigScope<E, T> {
  var forOutputs: OutputsScope<E>.() -> List<T> = { emptyList() }
  var connect: ConnectScope<E, T>.() -> List<NetNode> = { emptyList() }

  override fun forOutputs(op: OutputsScope<E>.() -> List<T>) {
    forOutputs = op
  }

  override fun connect(op: ConnectScope<E, T>.() -> List<NetNode>) {
    connect = op
  }
}

private class OutputsScopeImpl<E : PartExt>(
  override val self: TNetNode<E>,
  override val world: ServerWorld,
  override val pos: BlockPos
) : OutputsScope<E>

private class ConnectScopeImpl<E : PartExt, out T>(
  override val output: T,
  override val self: TNetNode<E>,
  override val world: ServerWorld,
  override val pos: BlockPos,
  override val nv: NodeView
) : ConnectScope<E, T>