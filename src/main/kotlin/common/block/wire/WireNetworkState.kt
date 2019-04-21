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
      for (node in net.collectNodesAt(pos).toSet()) {
        if (node.data.ext !in worldExts) {
          net.destroyNode(node)
        }
        new -= node.data.ext
      }
    }

    for (node in new) {
      val net = createNetwork()
      net.createNode(pos, node)
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

    for ((k, v) in networksInPos.entries()) {
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
  val graph = NetGraph()

  private val nodesInPos = HashMultimap.create<BlockPos, NetNode>()

  fun collectNodesAt(pos: BlockPos) = nodesInPos[pos]

  fun createNode(pos: BlockPos, ext: PartExt<out Any?>): NetNode {
    val node = graph.add(NetworkPart(pos, ext))
    nodesInPos.put(pos, node)
    controller.networksInPos.put(pos, this)
    println("Created node $node")
    return node
  }

  fun destroyNode(node: NetNode) {
    nodesInPos.remove(node.data.pos, node)
    graph.remove(node)
    println("Destroyed node $node")

    if (graph.nodes.isEmpty()) {
      controller.destroyNetwork(id)
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

interface PartExt<D> {
  val data: D

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}

fun ServerWorld.getWireNetworkState(): WireNetworkState {
  val dimension = getDimension()
  return persistentStateManager.getOrCreate({ WireNetworkState(this) }, WireNetworkState.getNameForDimension(dimension))
}