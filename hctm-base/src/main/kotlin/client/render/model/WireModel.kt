package net.dblsaiko.hctm.client.render.model

import net.dblsaiko.hctm.client.render.model.CenterVariant.Crossing
import net.dblsaiko.hctm.client.render.model.CenterVariant.Standalone
import net.dblsaiko.hctm.client.render.model.CenterVariant.Straight1
import net.dblsaiko.hctm.client.render.model.CenterVariant.Straight2
import net.dblsaiko.hctm.client.render.model.ExtVariant.Corner
import net.dblsaiko.hctm.client.render.model.ExtVariant.External
import net.dblsaiko.hctm.client.render.model.ExtVariant.Internal
import net.dblsaiko.hctm.client.render.model.ExtVariant.Terminal
import net.dblsaiko.hctm.client.render.model.ExtVariant.Unconnected
import net.dblsaiko.hctm.client.render.model.ExtVariant.UnconnectedCrossing
import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.block.Connection
import net.dblsaiko.hctm.common.block.ConnectionType.CORNER
import net.dblsaiko.hctm.common.block.ConnectionType.EXTERNAL
import net.dblsaiko.hctm.common.block.ConnectionType.INTERNAL
import net.dblsaiko.hctm.common.block.WireRepr
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelOverrideList
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
    context.popTransform()
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

  fun getWireState(world: BlockRenderView, pos: BlockPos, state: BlockState): Set<WireRepr> {
    return (world.getBlockEntity(pos) as? BaseWireBlockEntity)?.connections.orEmpty()
  }

  fun getItemWireState(): Set<WireRepr> {
    return setOf(
      WireRepr(
        side = Direction.DOWN,
        connections = setOf(
          Connection(edge = Direction.NORTH, type = EXTERNAL),
          Connection(edge = Direction.SOUTH, type = EXTERNAL),
          Connection(edge = Direction.WEST, type = EXTERNAL),
          Connection(edge = Direction.EAST, type = EXTERNAL)
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

  override fun getOverrides() = ModelOverrideList.EMPTY

}

data class WireModelParts(val sides: Map<Direction, WireModelPart>)

data class WireModelPart(val center: Map<CenterVariant, Mesh>, val exts: Map<Pair<Direction, ExtVariant>, Mesh>)