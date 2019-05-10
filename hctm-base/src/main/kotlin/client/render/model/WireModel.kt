package therealfarfetchd.hctm.client.render.model

import grondag.frex.api.mesh.Mesh
import grondag.frex.api.model.DynamicBakedModel
import grondag.frex.api.model.ModelHelper
import grondag.frex.api.render.RenderContext
import grondag.frex.api.render.TerrainBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis.X
import net.minecraft.util.math.Direction.Axis.Y
import net.minecraft.util.math.Direction.Axis.Z
import therealfarfetchd.hctm.client.render.model.CenterVariant.Crossing
import therealfarfetchd.hctm.client.render.model.CenterVariant.Standalone
import therealfarfetchd.hctm.client.render.model.CenterVariant.Straight1
import therealfarfetchd.hctm.client.render.model.CenterVariant.Straight2
import therealfarfetchd.hctm.client.render.model.ConnectionType.CORNER
import therealfarfetchd.hctm.client.render.model.ConnectionType.EXTERNAL
import therealfarfetchd.hctm.client.render.model.ConnectionType.INTERNAL
import therealfarfetchd.hctm.client.render.model.ExtVariant.Corner
import therealfarfetchd.hctm.client.render.model.ExtVariant.External
import therealfarfetchd.hctm.client.render.model.ExtVariant.Internal
import therealfarfetchd.hctm.client.render.model.ExtVariant.Terminal
import therealfarfetchd.hctm.client.render.model.ExtVariant.Unconnected
import therealfarfetchd.hctm.client.render.model.ExtVariant.UnconnectedCrossing
import therealfarfetchd.hctm.client.wire.ClientNetworkState
import therealfarfetchd.hctm.common.wire.WirePartExtType
import therealfarfetchd.retrocomputers.common.block.WireUtils
import java.util.*
import java.util.function.Supplier

class WireModel(
  val particle: Sprite,
  val parts: WireModelParts
) : BakedModel, DynamicBakedModel {

  override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
    emitQuads(getItemWireState(), context)
  }

  override fun emitBlockQuads(blockView: TerrainBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
    emitQuads(getWireState(pos, state), context)
  }

  fun emitQuads(state: Set<WireRepr>, context: RenderContext) {
    val meshConsumer = context.meshConsumer()

    for ((side, conns) in state) {
      val s = parts.sides.getValue(side)

      val cv = when (conns.size) {
        0 -> Standalone
        1 -> getCenterVariant(side, conns.first().edge)
        2 -> {
          val (first, second) = conns.toList()
          getCenterVariant(side, first.edge, second.edge)
        }
        else -> Crossing
      }

      fun getExtVariant(edge: Direction) =
        when (conns.firstOrNull { it.edge == edge }?.type) {
          INTERNAL -> Internal
          EXTERNAL -> External
          CORNER -> Corner
          null -> when (cv) {
            Crossing -> UnconnectedCrossing
            Straight1, Straight2 -> {
              if (conns.size == 2) Unconnected
              else {
                if (conns.first().edge.axis == edge.axis) Terminal
                else Unconnected
              }
            }
            Standalone -> {
              when (Pair(side.axis, edge.axis)) {
                Pair(X, Z), Pair(Z, X), Pair(Y, X) -> Terminal
                else -> Unconnected
              }
            }
          }
        }

      meshConsumer.accept(s.center.getValue(cv))
      for (edge in Direction.values().filter { it.axis != side.axis }) {
        meshConsumer.accept(s.exts.getValue(Pair(edge, getExtVariant(edge))))
      }
    }
  }

  @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
  private fun getCenterVariant(side: Direction, edge: Direction): CenterVariant = when (side.axis) {
    X -> when (edge.axis) {
      X -> error("unreachable")
      Y -> Straight2
      Z -> Straight1
    }
    Y -> when (edge.axis) {
      X -> Straight1
      Y -> error("unreachable")
      Z -> Straight2
    }
    Z -> when (edge.axis) {
      X -> Straight1
      Y -> Straight2
      Z -> error("unreachable")
    }
  }

  private fun getCenterVariant(side: Direction, edge1: Direction, edge2: Direction): CenterVariant =
    if (edge1.axis == edge2.axis) getCenterVariant(side, edge1) else Crossing

  override fun getQuads(state: BlockState?, face: Direction?, rnd: Random): List<BakedQuad> {
    return emptyList()
  }

  fun getWireState(pos: BlockPos, state: BlockState): Set<WireRepr> {
    return shittyGetWireState(pos, state)
  }

  fun shittyGetWireState(pos: BlockPos, state: BlockState): Set<WireRepr> {
    val net = ClientNetworkState.request(MinecraftClient.getInstance().world) ?: return emptySet()
    val nodes = net.getNodesAt(pos)
    val connMap = nodes.associate { node -> (node.data.ext as? WirePartExtType)?.side to node.connections.mapNotNull { it.other(node).data.pos.subtract(node.data.pos).let { Direction.fromVector(it.x, it.y, it.z) } } }
    return WireUtils.getOccupiedSides(state).map { side ->
      WireRepr(side, connMap[side]?.map { Connection(it, EXTERNAL) }?.toSet().orEmpty())
    }.toSet()
  }

  fun getItemWireState(): Set<WireRepr> {
    return setOf(
      WireRepr(
        side = Direction.DOWN,
        connections = setOf(
          Connection(edge = Direction.NORTH, type = ConnectionType.EXTERNAL),
          Connection(edge = Direction.SOUTH, type = ConnectionType.EXTERNAL),
          Connection(edge = Direction.WEST, type = ConnectionType.EXTERNAL),
          Connection(edge = Direction.EAST, type = ConnectionType.EXTERNAL)
        )
      )
    )
  }

  override fun getSprite() = particle

  override fun getTransformation() = ModelHelper.MODEL_TRANSFORM_BLOCK

  override fun useAmbientOcclusion() = true

  override fun hasDepthInGui() = true

  override fun isBuiltin() = false

  override fun isVanillaAdapter() = false

  override fun getItemPropertyOverrides() = ModelItemPropertyOverrideList.EMPTY

}

data class WireRepr(val side: Direction, val connections: Set<Connection>)

data class Connection(val edge: Direction, val type: ConnectionType)

enum class ConnectionType {
  INTERNAL,
  EXTERNAL,
  CORNER,
}

data class WireModelParts(val sides: Map<Direction, WireModelPart>)

data class WireModelPart(val center: Map<CenterVariant, Mesh>, val exts: Map<Pair<Direction, ExtVariant>, Mesh>)