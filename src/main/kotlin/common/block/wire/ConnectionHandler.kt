package therealfarfetchd.retrocomputers.common.block.wire

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

/**
 * Create a connection handler from DSL. Provides tryConnect method for use in PartExt
 */
fun <E : PartExt, T> connectionHandler(op: ConnectionHandlerScope<E, T>.() -> Unit): ConnectionHandler<E> {
  val sc = ConnectionHandlerScopeImpl<E, T>().also(op)
  return ConnectionHandlerImpl(sc.parts)
}

interface ConnectionHandler<E : PartExt> {
  fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode>
}

private class ConnectionHandlerImpl<E : PartExt, T>(private val parts: List<ConnectionHandlerPart<E, T>>) : ConnectionHandler<E> {

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    self as TNetNode<E> // yep, this is unsafe, but if you pass the wrong type of NetNode here you're a dumbass
    val nodes = mutableSetOf<NetNode>()
    val triedOutputs = mutableListOf<T>()
    for (p in parts) {
      val outs = p.getPotentialOutputs(self, world, pos)
      for (out in outs - triedOutputs) {
        val node = p.tryConnect(out, self, world, pos, nv)
        if (node != null) {
          triedOutputs += out
          nodes += node
        }
      }
    }
    return nodes
  }

}

private abstract class ConnectionHandlerPart<E : PartExt, T> {
  abstract fun getPotentialOutputs(self: TNetNode<E>, world: ServerWorld, pos: BlockPos): List<T>

  abstract fun tryConnect(output: T, self: TNetNode<E>, world: ServerWorld, pos: BlockPos, nv: NodeView): NetNode?
}

interface ConnectionHandlerScope<E : PartExt, T> {
  fun connectionRule(op: PartConfigScope<E, T>.() -> Unit)
}

interface PartConfigScope<E : PartExt, T> {
  fun forOutputs(op: OutputsScope<E>.() -> List<T>)
  fun connect(op: ConnectScope<E, T>.() -> NetNode?)
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

private class ConnectionHandlerScopeImpl<E : PartExt, T> : ConnectionHandlerScope<E, T> {
  val parts = mutableListOf<ConnectionHandlerPart<E, T>>()

  override fun connectionRule(op: PartConfigScope<E, T>.() -> Unit) {
    val pcs = PartConfigScopeImpl<E, T>().also(op)
    parts += object : ConnectionHandlerPart<E, T>() {
      override fun getPotentialOutputs(self: TNetNode<E>, world: ServerWorld, pos: BlockPos): List<T> =
        pcs.forOutputs(OutputsScopeImpl(self, world, pos))

      override fun tryConnect(output: T, self: TNetNode<E>, world: ServerWorld, pos: BlockPos, nv: NodeView): NetNode? =
        pcs.connect(ConnectScopeImpl(output, self, world, pos, nv))
    }
  }
}

private class PartConfigScopeImpl<E : PartExt, T> : PartConfigScope<E, T> {
  var forOutputs: OutputsScope<E>.() -> List<T> = { emptyList() }
  var connect: ConnectScope<E, T>.() -> NetNode? = { null }

  override fun forOutputs(op: OutputsScope<E>.() -> List<T>) {
    forOutputs = op
  }

  override fun connect(op: ConnectScope<E, T>.() -> NetNode?) {
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