package therealfarfetchd.hctm.common.wire

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

/**
 * Create a connection handler from DSL. Provides tryConnect method for use in PartExt
 */
fun <E : PartExt, T, D> connectionHandler(op: ConnectionHandlerScope<E, T, D>.() -> Unit): ConnectionHandler<E, D> {
  val sc = ConnectionHandlerScopeImpl<E, T, D>().also(op)
  return ConnectionHandlerImpl(sc.parts)
}

interface ConnectionHandler<E : PartExt, D> {
  fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView, data: D): Set<NetNode>
}

private class ConnectionHandlerImpl<E : PartExt, T, D>(private val parts: List<ConnectionHandlerPart<E, T, D>>) : ConnectionHandler<E, D> {

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView, data: D): Set<NetNode> {
    self as TNetNode<E> // yep, this is unsafe, but if you pass the wrong type of NetNode here you're a dumbass
    val nodes = mutableSetOf<NetNode>()
    val triedOutputs = mutableListOf<T>()
    for (p in parts) {
      val outs = p.getPotentialOutputs(self, world, pos, data)
      for (out in outs - triedOutputs) {
        val node = p.tryConnect(out, self, world, pos, nv, data)
        if (node != null) {
          triedOutputs += out
          nodes += node
        }
      }
    }
    return nodes
  }

}

private abstract class ConnectionHandlerPart<E : PartExt, T, D> {
  abstract fun getPotentialOutputs(self: TNetNode<E>, world: ServerWorld, pos: BlockPos, data: D): List<T>

  abstract fun tryConnect(output: T, self: TNetNode<E>, world: ServerWorld, pos: BlockPos, nv: NodeView, data: D): NetNode?
}

interface ConnectionHandlerScope<E : PartExt, T, D> {
  fun connectionRule(op: PartConfigScope<E, T, D>.() -> Unit)
}

interface PartConfigScope<E : PartExt, T, D> {
  fun forOutputs(op: OutputsScope<E, D>.() -> List<T>)
  fun connect(op: ConnectScope<E, T, D>.() -> NetNode?)
}

interface OutputsScope<E : PartExt, D> {
  val self: TNetNode<E>
  val world: ServerWorld
  val pos: BlockPos
  val data: D
}

interface ConnectScope<E : PartExt, out T, D> {
  val output: T
  val self: TNetNode<E>
  val world: ServerWorld
  val pos: BlockPos
  val nv: NodeView
  val data: D
}

private class ConnectionHandlerScopeImpl<E : PartExt, T, D> : ConnectionHandlerScope<E, T, D> {
  val parts = mutableListOf<ConnectionHandlerPart<E, T, D>>()

  override fun connectionRule(op: PartConfigScope<E, T, D>.() -> Unit) {
    val pcs = PartConfigScopeImpl<E, T, D>().also(op)
    parts += object : ConnectionHandlerPart<E, T, D>() {
      override fun getPotentialOutputs(self: TNetNode<E>, world: ServerWorld, pos: BlockPos, data: D): List<T> =
        pcs.forOutputs(OutputsScopeImpl(self, world, pos, data))

      override fun tryConnect(output: T, self: TNetNode<E>, world: ServerWorld, pos: BlockPos, nv: NodeView, data: D): NetNode? =
        pcs.connect(ConnectScopeImpl(output, self, world, pos, nv, data))
    }
  }
}

private class PartConfigScopeImpl<E : PartExt, T, D> : PartConfigScope<E, T, D> {
  var forOutputs: OutputsScope<E, D>.() -> List<T> = { emptyList() }
  var connect: ConnectScope<E, T, D>.() -> NetNode? = { null }

  override fun forOutputs(op: OutputsScope<E, D>.() -> List<T>) {
    forOutputs = op
  }

  override fun connect(op: ConnectScope<E, T, D>.() -> NetNode?) {
    connect = op
  }
}

private class OutputsScopeImpl<E : PartExt, D>(
  override val self: TNetNode<E>,
  override val world: ServerWorld,
  override val pos: BlockPos,
  override val data: D
) : OutputsScope<E, D>

private class ConnectScopeImpl<E : PartExt, out T, D>(
  override val output: T,
  override val self: TNetNode<E>,
  override val world: ServerWorld,
  override val pos: BlockPos,
  override val nv: NodeView,
  override val data: D
) : ConnectScope<E, T, D>