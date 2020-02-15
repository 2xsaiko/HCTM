package net.dblsaiko.hctm.client.render.model

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.block.Connection
import net.dblsaiko.hctm.common.block.ConnectionType
import net.dblsaiko.hctm.common.block.WireRepr
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
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
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

class WireModel(
  val particle: Sprite,
  val parts: WireModelParts
) : BakedModel, FabricBakedModel {

  override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
    emitQuads(getItemWireState(), context)
  }

  override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
    emitQuads(getWireState(blockView, pos, state), context)
  }

  fun emitQuads(state: Set<WireRepr>, context: RenderContext) {
    context.pushTransform { quad ->
      quad.spriteBake(0, particle, MutableQuadView.BAKE_NORMALIZED)

      true
    }

    val meshConsumer = context.meshConsumer()

    for ((side, conns) in state) {
      val s = parts.sides.getValue(side)

      val cv = when (conns.size) {
        0 -> CenterVariant.STANDALONE
        1 -> getCenterVariant(side, conns.first().edge)
        2 -> {
          val (first, second) = conns.toList()
          getCenterVariant(side, first.edge, second.edge)
        }
        else -> CenterVariant.CROSSING
      }

      fun getExtVariant(edge: Direction) =
        when (conns.firstOrNull { it.edge == edge }?.type) {
          ConnectionType.INTERNAL -> ExtVariant.INTERNAL
          ConnectionType.EXTERNAL -> ExtVariant.EXTERNAL
          ConnectionType.CORNER -> ExtVariant.CORNER
          null -> when (cv) {
            CenterVariant.CROSSING -> ExtVariant.UNCONNECTED_CROSSING
            CenterVariant.STRAIGHT_1, CenterVariant.STRAIGHT_2 -> {
              if (conns.size == 2) ExtVariant.UNCONNECTED
              else {
                if (conns.first().edge.axis == edge.axis) ExtVariant.TERMINAL
                else ExtVariant.UNCONNECTED
              }
            }
            CenterVariant.STANDALONE -> {
              when (Pair(side.axis, edge.axis)) {
                Pair(X, Z), Pair(Z, X), Pair(Y, X) -> ExtVariant.TERMINAL
                else -> ExtVariant.UNCONNECTED
              }
            }
          }
        }

      meshConsumer.accept(s.center.getValue(cv))
      for (edge in Direction.values().filter { it.axis != side.axis }) {
        meshConsumer.accept(s.exts.getValue(Pair(edge, getExtVariant(edge))))
      }
    }
    context.popTransform()
  }

  @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
  private fun getCenterVariant(side: Direction, edge: Direction): CenterVariant = when (side.axis) {
    X -> when (edge.axis) {
      X -> error("unreachable")
      Y -> CenterVariant.STRAIGHT_2
      Z -> CenterVariant.STRAIGHT_1
    }
    Y -> when (edge.axis) {
      X -> CenterVariant.STRAIGHT_1
      Y -> error("unreachable")
      Z -> CenterVariant.STRAIGHT_2
    }
    Z -> when (edge.axis) {
      X -> CenterVariant.STRAIGHT_1
      Y -> CenterVariant.STRAIGHT_2
      Z -> error("unreachable")
    }
  }

  private fun getCenterVariant(side: Direction, edge1: Direction, edge2: Direction): CenterVariant =
    if (edge1.axis == edge2.axis) getCenterVariant(side, edge1) else CenterVariant.CROSSING

  override fun getQuads(state: BlockState?, face: Direction?, rnd: Random): List<BakedQuad> {
    return emptyList()
  }

  fun getWireState(world: BlockRenderView, pos: BlockPos, state: BlockState): Set<WireRepr> {
    return (world.getBlockEntity(pos) as? BaseWireBlockEntity)?.connections.orEmpty()
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

  override fun hasDepth() = true

  override fun isSideLit() = true

  override fun isBuiltin() = false

  override fun isVanillaAdapter() = false

  override fun getItemPropertyOverrides() = ModelItemPropertyOverrideList.EMPTY

}

data class WireModelParts(val sides: Map<Direction, WireModelPart>)

data class WireModelPart(val center: Map<CenterVariant, Mesh>, val exts: Map<Pair<Direction, ExtVariant>, Mesh>)