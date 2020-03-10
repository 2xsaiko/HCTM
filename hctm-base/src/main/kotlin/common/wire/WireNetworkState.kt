package net.dblsaiko.hctm.common.wire

import com.google.common.collect.HashMultimap
import net.dblsaiko.hctm.common.graph.Graph
import net.dblsaiko.hctm.common.graph.Link
import net.dblsaiko.hctm.common.graph.Node
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import net.minecraft.world.dimension.Dimension
import java.util.*

typealias NetNode = Node<NetworkPart<out PartExt>, Nothing?>
typealias NetGraph = Graph<NetworkPart<out PartExt>, Nothing?>
typealias NetLink = Link<NetworkPart<out PartExt>, Nothing?>

typealias TNetNode<T> = Node<NetworkPart<T>, Nothing>

class WireNetworkState(val world: ServerWorld) : PersistentState(getNameForDimension(world.getDimension())) {
  var controller = WireNetworkController(::markDirty, world)

  override fun toTag(tag: CompoundTag): CompoundTag {
    return tag.copyFrom(controller.toTag(world))
  }

  override fun fromTag(tag: CompoundTag) {
    controller = WireNetworkController.fromTag(tag, world)
    controller.changeListener = ::markDirty
  }

  companion object {
    fun getNameForDimension(dimension: Dimension) = "wirenet${dimension.type.suffix}"
  }
}

class WireNetworkController(var changeListener: () -> Unit = {}, internal val world: ServerWorld? = null) {
  private val networks = mutableMapOf<UUID, Network>()
  @JvmSynthetic internal val networksInPos = HashMultimap.create<BlockPos, Network>()
  @JvmSynthetic internal val nodesToNetworks = mutableMapOf<NetNode, UUID>()

  private var changed = setOf<NetNode>()

  fun onBlockChanged(world: ServerWorld, pos: BlockPos, state: BlockState) {
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
      val node = net.createNode(pos.toImmutable(), ext)
      updateNodeConnections(world, node)
    }
  }

  fun getNodesAt(pos: BlockPos): Set<NetNode> {
    return networksInPos[pos].flatMap { net -> net.getNodesAt(pos).map { it } }.toSet()
  }

  fun getNetworksAt(pos: BlockPos): Set<Network> {
    return networksInPos[pos]
  }

  fun getBlockType(world: World, node: NetNode): Block {
    return world.getBlockState(node.data.pos).block
  }

  fun getNetworks() = networks.values.toSet()

  fun updateNodeConnections(world: ServerWorld, node: NetNode) {
    changeListener()
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

  fun getNetwork(id: UUID): Network? = networks[id]

  fun createNetwork(): Network {
    changeListener()
    val net = Network(this, UUID.randomUUID())
    networks += net.id to net
    return net
  }

  fun destroyNetwork(id: UUID) {
    changeListener()
    networks -= id

    for ((k, v) in networksInPos.entries().toSet()) {
      if (v.id == id) networksInPos.remove(k, v)
    }

    nodesToNetworks -= nodesToNetworks.filter { it.value == id }.keys
  }

  fun rebuildRefs(vararg networks: UUID) {
    changeListener()
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

  fun cleanup() {
    for (net in networks.values.toSet()) {
      if (net.getNodes().isEmpty()) {
        destroyNetwork(net.id)
      }
    }
  }

  fun toTag(world: World): CompoundTag {
    val tag = CompoundTag()
    val list = ListTag()
    networks.values.map { it.toTag(world, CompoundTag()) }.forEach { list.add(it) }
    tag.put("networks", list)
    return tag
  }

  fun scheduleUpdate(node: Node<NetworkPart<out PartExt>, Nothing?>) {
    changed += node
  }

  fun flushUpdates() {
    while (changed.isNotEmpty()) {
      val n = changed.first()
      world?.also { n.data.ext.onChanged(n, world, n.data.pos) }
      changed -= n
    }
  }

  companion object {
    fun fromTag(tag: CompoundTag, world: ServerWorld? = null): WireNetworkController {
      val controller = WireNetworkController(world = world)

      val sNetworks = tag.getList("networks", NbtType.COMPOUND)
      for (sNetwork in sNetworks.map { it as CompoundTag }) {
        val net = Network.fromTag(controller, sNetwork) ?: continue
        controller.networks += net.id to net
      }
      controller.rebuildRefs()
      controller.cleanup()
      return controller
    }
  }

}

class Network(val controller: WireNetworkController, val id: UUID) {
  private val graph = NetGraph()

  private val nodesInPos = HashMultimap.create<BlockPos, NetNode>()

  fun getNodesAt(pos: BlockPos) = nodesInPos[pos].toSet()

  fun createNode(pos: BlockPos, ext: PartExt): NetNode {
    controller.changeListener()
    val node = graph.add(NetworkPart(pos, ext))
    nodesInPos.put(pos, node)
    controller.networksInPos.put(pos, this)
    controller.nodesToNetworks[node] = this.id
    controller.scheduleUpdate(node)
    return node
  }

  fun destroyNode(node: NetNode) {
    controller.changeListener()
    val connected = node.connections.map { it.other(node) }
    graph.remove(node)
    controller.scheduleUpdate(node)
    for (other in connected) controller.scheduleUpdate(other)

    split().forEach { controller.rebuildRefs(it.id) }

    if (graph.nodes.isEmpty()) controller.destroyNetwork(id)
    controller.rebuildRefs(id)
  }

  fun link(node1: NetNode, node2: NetNode) {
    graph.link(node1, node2, null)
    controller.scheduleUpdate(node1)
    controller.scheduleUpdate(node2)
  }

  fun merge(other: Network) {
    controller.changeListener()
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

    if (newGraphs.isNotEmpty()) {
      controller.changeListener()

      val networks = newGraphs.map {
        val net = controller.createNetwork()
        net.graph.join(it)
        net
      }

      networks.forEach { controller.rebuildRefs(it.id) }
      controller.rebuildRefs(id)

      return networks.toSet()
    }

    return emptySet()
  }

  fun rebuildRefs() {
    controller.changeListener()
    nodesInPos.clear()
    for (node in graph.nodes) {
      nodesInPos.put(node.data.pos, node)
    }
  }

  fun toTag(world: World, tag: CompoundTag): CompoundTag {
    val serializedNodes = mutableListOf<CompoundTag>()
    val serializedLinks = mutableListOf<CompoundTag>()
    val nodes = graph.nodes.toList()
    val n1 = nodes.withIndex().associate { it.value to it.index }
    for (node in nodes) {
      serializedNodes += node.data.toTag(controller.getBlockType(world, node), CompoundTag())
    }
    for (link in nodes.flatMap { it.connections }.distinct()) {
      val sLink = CompoundTag()
      sLink.putInt("first", n1.getValue(link.first))
      sLink.putInt("second", n1.getValue(link.second))
      // sLink.put("data", link.data.toTag())
      serializedLinks += sLink
    }
    tag.put("nodes", ListTag().also { t -> serializedNodes.forEach { t.add(it) } })
    tag.put("links", ListTag().also { t -> serializedLinks.forEach { t.add(it) } })
    tag.putUuidNew("id", id)
    return tag
  }

  companion object {
    fun fromTag(controller: WireNetworkController, tag: CompoundTag): Network? {
      val id = if (tag.containsUuidOld("id")) {
        tag.getUuidOld("id")
      } else {
        tag.getUuidNew("id")
      }
      val network = Network(controller, id)
      val sNodes = tag.getList("nodes", NbtType.COMPOUND)
      val sLinks = tag.getList("links", NbtType.COMPOUND)

      val nodes = mutableListOf<NetNode?>()

      for (node in sNodes.map { it as CompoundTag }) {
        val part = NetworkPart.fromTag(node)
        if (part == null) {
          nodes += null as NetNode?
          continue
        }
        nodes += network.createNode(part.pos, part.ext)
      }

      for (link in sLinks.map { it as CompoundTag }) {
        val first = nodes[link.getInt("first")]
        val second = nodes[link.getInt("second")]
        // val data = /* something */
        if (first != null && second != null) {
          network.graph.link(first, second, null)
        }
      }

      network.rebuildRefs()

      return network
    }
  }

}

data class NetworkPart<T : PartExt>(var pos: BlockPos, val ext: T) {
  fun toTag(block: Block, tag: CompoundTag): CompoundTag {
    tag.putInt("x", pos.x)
    tag.putInt("y", pos.y)
    tag.putInt("z", pos.z)
    tag.put("ext", ext.toTag())
    tag.putString("block", Registry.BLOCK.getId(block).toString())
    return tag
  }

  companion object {
    fun fromTag(tag: CompoundTag): NetworkPart<PartExt>? {
      val block = Registry.BLOCK[Identifier(tag.getString("block"))]
      val extTag = tag["ext"]
      if (block is BlockPartProvider && extTag != null) {
        val ext = block.createExtFromTag(extTag) ?: return null
        val pos = BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))
        return NetworkPart(pos, ext)
      } else return null
    }
  }
}

interface BlockPartProvider {
  fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt>

  fun createExtFromTag(tag: Tag): PartExt?
}

/**
 * This must be immutable and have equals/hashCode implemented correctly.
 * You **can** store data here, but again, it must be immutable, and hashed correctly.
 * Kotlin's data class with only `val`s used should do all this automatically, so use that.
 */
interface PartExt {
  /**
   * Return the nodes that this node wants to connect to.
   * Will only actually connect if other node also wants to connect to this
   */
  fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode>

  fun toTag(): Tag

  /**
   * Node created, removed, connected, disconnected
   */
  @JvmDefault
  fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
  }

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}

class NodeView(world: ServerWorld) {
  private val wns = world.getWireNetworkState()

  fun getNodes(pos: BlockPos): Set<NetNode> = wns.controller.getNodesAt(pos)
}

fun ServerWorld.getWireNetworkState(): WireNetworkState {
  val dimension = getDimension()
  return persistentStateManager.getOrCreate({ WireNetworkState(this) }, WireNetworkState.getNameForDimension(dimension))
}