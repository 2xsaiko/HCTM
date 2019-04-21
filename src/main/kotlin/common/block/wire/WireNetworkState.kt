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

typealias NetNode = Node<NetworkPart<out PartExt<out Any?>>, Nothing?>
typealias NetGraph = Graph<NetworkPart<out PartExt<out Any?>>, Nothing?>

class WireNetworkState(private val world: ServerWorld) : PersistentState(getNameForDimension(world.getDimension())) {
  private val networks = mutableMapOf<UUID, Network>()
  @JvmSynthetic internal val networksInPos = HashMultimap.create<BlockPos, Network>()
  @JvmSynthetic internal val nodesToNetworks = mutableMapOf<NetNode, UUID>()

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

    for (ext in new) {
      val net = createNetwork()
      val node = net.createNode(pos, ext)
      updateNodeConnections(node)
    }
  }

  fun getNodesAt(pos: BlockPos): Set<NetNode> {
    return networksInPos.values().flatMap { net -> net.getNodesAt(pos).map { it } }.toSet()
  }

  fun updateNodeConnections(node: NetNode) {
    val nodeNetId = getNetIdForNode(node)

    val nv = NodeView(world)
    val ids = node.data.ext.tryConnect(node, world, node.data.pos, nv)
    val oldConnections = node.connections.map { it.other(node) }
    val potentialNewConnections = ids.filter { getNetIdForNode(it) != nodeNetId || it !in oldConnections }
    val newConnections = potentialNewConnections.filter { node in it.data.ext.tryConnect(it, world, it.data.pos, nv) }

    for (other in newConnections) {
      val net = networks.getValue(nodeNetId)
      if (getNetIdForNode(other) != nodeNetId) {
        val otherNet = networks.getValue(getNetIdForNode(other))
        net.merge(otherNet)
      }

      net.link(node, other)
    }
  }

  fun getNetIdForNode(node: NetNode) = nodesToNetworks.getValue(node)

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

    nodesToNetworks -= nodesToNetworks.filter { it.value == id }.keys

    println("Network $id destroyed")
  }

  fun rebuildRefs(vararg networks: UUID) {
    val toRebuild = networks.takeIf { it.isNotEmpty() }?.map { Pair(it, this.networks[it]) } ?: this.networks.entries.map { Pair(it.key, it.value) }

    for ((id, net) in toRebuild) {
      for ((pos, net) in networksInPos.entries().toSet()) {
        if (net.id == id) networksInPos.remove(pos, net)
      }

      nodesToNetworks -= nodesToNetworks.filterValues { it == id }.keys

      if (net != null) {
        net.rebuildRefs()
        net.getNodes()
          .onEach { nodesToNetworks[it] = net.id }
          .map { it.data.pos }.toSet()
          .forEach { networksInPos.put(it, net) }
      }
    }
  }

  override fun toTag(tag: CompoundTag): CompoundTag {
    return tag
  }

  override fun fromTag(tag: CompoundTag) {
    rebuildRefs()
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
    controller.nodesToNetworks[node] = this.id
    println("Created node $node")
    return node
  }

  fun destroyNode(node: NetNode) {
    graph.remove(node)
    println("Destroyed node $node")

    split().forEach { controller.rebuildRefs(it.id) }

    if (graph.nodes.isEmpty()) controller.destroyNetwork(id)
    controller.rebuildRefs(id)
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
      controller.nodesToNetworks += graph.nodes.associate { it to this.id }
      controller.destroyNetwork(other.id)
    }
  }

  fun getNodes() = graph.nodes

  fun split(): Set<Network> {
    val newGraphs = graph.split()

    val networks = newGraphs.map {
      val net = controller.createNetwork()
      net.graph.join(it)
      net
    }

    networks.forEach { controller.rebuildRefs(it.id) }
    controller.rebuildRefs(id)

    return networks.toSet()
  }

  fun rebuildRefs() {
    nodesInPos.clear()
    for (node in graph.nodes) {
      nodesInPos.put(node.data.pos, node)
    }
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
  fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode>

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}

class NodeView(world: ServerWorld) {
  private val wns = world.getWireNetworkState()

  fun getNodes(pos: BlockPos): Set<NetNode> = wns.getNodesAt(pos)
}

fun ServerWorld.getWireNetworkState(): WireNetworkState {
  val dimension = getDimension()
  return persistentStateManager.getOrCreate({ WireNetworkState(this) }, WireNetworkState.getNameForDimension(dimension))
}