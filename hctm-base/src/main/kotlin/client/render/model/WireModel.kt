package therealfarfetchd.hctm.client.render.model

import grondag.frex.api.mesh.Mesh
import grondag.frex.api.model.DynamicBakedModel
import grondag.frex.api.model.ModelHelper
import grondag.frex.api.render.RenderContext
import grondag.frex.api.render.TerrainBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import therealfarfetchd.hctm.client.render.model.ConnectionType.EXTERNAL
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
      meshConsumer.accept(s.center)
      for ((edge, type) in conns) {
        meshConsumer.accept(s.exts.getValue(Pair(edge, type)))
      }
    }
  }

  override fun getQuads(state: BlockState?, face: Direction?, rnd: Random): List<BakedQuad> {
    return emptyList()
  }

  fun getWireState(pos: BlockPos, state: BlockState): Set<WireRepr> {
    return shittyGetWireState(pos, state)
  }

  fun shittyGetWireState(pos: BlockPos, state: BlockState): Set<WireRepr> {
    // val net = ClientNetworkState.request(MinecraftClient.getInstance().world) ?: return emptySet()
    // val nodes = net.getNodesAt(pos)
    return WireUtils.getOccupiedSides(state).map { side ->
      WireRepr(side, Direction.values().filter { it.axis != side.axis }.map { Connection(it, EXTERNAL) }.toSet())
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

data class WireModelPart(val center: Mesh, val exts: Map<Pair<Direction, ConnectionType>, Mesh>)