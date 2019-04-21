package therealfarfetchd.retrocomputers.common.block.wire

import com.google.common.collect.HashMultimap
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import net.minecraft.world.dimension.Dimension
import therealfarfetchd.retrocomputers.common.graph.Graph
import therealfarfetchd.retrocomputers.common.graph.Node
import java.util.*

private typealias NetNode = Node<NetworkPart<out PartExt<out Any?>>, Nothing?>
private typealias NetGraph = Graph<NetworkPart<out PartExt<out Any?>>, Nothing?>

class WireNetworkState(private val world: ServerWorld) : PersistentState(getNameForDimension(world.getDimension())) {
  private val networks = mutableMapOf<UUID, Network>()
  @JvmSynthetic internal val networksInPos = HashMultimap.create<BlockPos, Network>()

  fun onBlockChanged(pos: BlockPos, state: BlockState) {
    val actualState = world.getBlockState(pos)
    val worldExts = (actualState.block as? BlockPartProvider)?.getPartsInBlock(world, pos, actualState).orEmpty()

    val new = worldExts.toMutableSet()

    for (net in networksInPos[pos].toSet()) {
      for (node in net.getNodesAt(pos)) {
        if (node.data.ext !in worldExts) {
          net.destroyNode(node)
        }
        new -= node.data.ext
      }
    }

    for (node in new) {
      val net = createNetwork()
      val node = net.createNode(pos, node)
      updateNodeConnections(IdNode(net.id, node))
    }
  }

  fun getNodesAt(pos: BlockPos): Set<IdNode> {
    return networksInPos.values().flatMap { net -> net.getNodesAt(pos).map { IdNode(net.id, it) } }.toSet()
  }

  fun updateNodeConnections(node: IdNode) {
    val nv = NodeView(world)
    val ids = node.node.data.ext.tryConnect(node, world, node.node.data.pos, nv)
    val oldConnections = node.node.connections.map { it.other(node.node) }
    val potentialNewConnections = ids.filter { it.net != node.net || it.node !in oldConnections }
    var newConnections = potentialNewConnections.filter { node in it.node.data.ext.tryConnect(it, world, it.node.data.pos, nv) }

    while(newConnections.isNotEmpty())
    for (other in newConnections) {
      val net = networks.getValue(node.net)
      if (other.net != node.net) {
        val otherNet = networks.getValue(other.net)
        net.merge(otherNet)
        newConnections = newConnections.map { if (it.net == otherNet.id) it.copy(net = net.id) else it }
        break
      }

      net.link(node.node, other.node)
      newConnections -= other
    }
  }

  fun createNetwork(): Network {
    val net = Network(this, UUID.randomUUID())
    networks += net.id to net
    println("Network ${net.id} created")
    return net
  }

  fun destroyNetwork(id: UUID) {
    networks[id]?.destroy()
    networks -= id

    for ((k, v) in networksInPos.entries().toSet()) {
      if (v.id == id) networksInPos.remove(k, v)
    }

    println("Network $id destroyed")
  }

  override fun toTag(tag: CompoundTag): CompoundTag {
    return tag
  }

  override fun fromTag(tag: CompoundTag) {

  }

  companion object {
    fun getNameForDimension(dimension: Dimension) = "wirenet${dimension.type.suffix}"
  }

}

class Network(val controller: WireNetworkState, val id: UUID) {
  private val graph = NetGraph()

  private val nodesInPos = HashMultimap.create<BlockPos, NetNode>()

  fun getNodesAt(pos: BlockPos) = nodesInPos[pos].toSet()

  fun createNode(pos: BlockPos, ext: PartExt<out Any?>): NetNode {
    val node = graph.add(NetworkPart(pos, ext))
    nodesInPos.put(pos, node)
    controller.networksInPos.put(pos, this)
    println("Created node $node")
    return node
  }

  fun destroyNode(node: NetNode) {
    // TODO split network if necessary
    nodesInPos.remove(node.data.pos, node)
    graph.remove(node)
    println("Destroyed node $node")

    if (graph.nodes.isEmpty()) {
      controller.destroyNetwork(id)
    }
  }

  fun link(node1: NetNode, node2: NetNode) {
    graph.link(node1, node2, null)
  }

  fun merge(other: Network) {
    if (other.id != id) {
      graph.join(other.graph)
      nodesInPos.putAll(other.nodesInPos)
      for (key in controller.networksInPos.keySet()) {
        controller.networksInPos.replaceValues(key, controller.networksInPos.get(key).map { if (it == other) this else it }.toSet())
      }
      controller.destroyNetwork(other.id)
    }
  }

  fun split(): Set<Network> {
    return setOf(this) // TODO
  }

  fun destroy() {

  }
}

data class NetworkPart<T : PartExt<out Any?>>(var pos: BlockPos, val ext: T) {
  fun toTag(tag: CompoundTag): CompoundTag {
    return tag
  }

  fun fromTag(tag: CompoundTag) {

  }
}

interface BlockPartProvider {
  fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt<out Any?>>
}

/**
 * This must be immutable and have equals/hashCode implemented correctly.
 */
interface PartExt<D> {
  /**
   * Implementation-specific data for this node.
   * Does this even need to be accessed by common code and therefore exist?
   */
  val data: D

  /**
   * Return the nodes that this node wants to connect to.
   * Will only actually connect if other node also wants to connect to this
   */
  fun tryConnect(self: IdNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<IdNode>

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}

class NodeView(world: ServerWorld) {
  private val wns = world.getWireNetworkState()

  fun getNodes(pos: BlockPos): Set<IdNode> = wns.getNodesAt(pos)
}

/**
 * A node with a network ID attached, because that isn't saved in the node itself for simplicity reasons.
 */
data class IdNode(val net: UUID, val node: NetNode)

fun ServerWorld.getWireNetworkState(): WireNetworkState {
  val dimension = getDimension()
  return persistentStateManager.getOrCreate({ WireNetworkState(this) }, WireNetworkState.getNameForDimension(dimension))
}