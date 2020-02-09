package net.dblsaiko.rswires.client.render.model

import com.mojang.datafixers.util.Pair
import net.dblsaiko.rswires.common.block.GateProperties
import net.dblsaiko.rswires.common.util.getRotationFor
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockRenderView
import therealfarfetchd.qcommon.croco.Vec3
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class GateModel(val wrapped: UnbakedModel) : UnbakedModel {

  val map = IdentityHashMap<BakedModel, Baked>()

  override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel? {
    return map.computeIfAbsent(wrapped.bake(loader, textureGetter, rotationContainer, modelId) ?: return null, ::Baked)
  }

  override fun getModelDependencies(): Collection<Identifier> {
    return wrapped.modelDependencies
  }

  override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: Set<Pair<String, String>>): Collection<SpriteIdentifier> {
    return wrapped.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
  }

  class Baked(val wrapped: BakedModel) : FabricBakedModel, BakedModel {

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
      val side = state[Properties.FACING]
      val rotation = state[GateProperties.Rotation]
      val (mat, rotationMat) = getRotationFor(side, rotation)

      context.pushTransform { quad ->
        quad as MutableQuadViewImpl
        ColorHelper.applyDiffuseShading(quad, true)
        for (idx in 0..3) {
          val newPos = mat.mul(Vec3(quad.posByIndex(idx, 0), quad.posByIndex(idx, 1), quad.posByIndex(idx, 2)))
          quad.pos(idx, newPos.x, newPos.y, newPos.z)
        }
        ColorHelper.applyDiffuseShading(quad, false)

        quad.cullFace()?.also { quad.cullFace(rotationMat.mul(Vec3.from(it.vector)).let { Direction.getFacing(it.x, it.y, it.z) }) }
        true
      }
      context.fallbackConsumer().accept(wrapped)
      context.popTransform()
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
      context.fallbackConsumer().accept(wrapped)
    }

    override fun isVanillaAdapter(): Boolean {
      return false
    }

    override fun getItemPropertyOverrides(): ModelItemPropertyOverrideList {
      return wrapped.itemPropertyOverrides
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random): List<BakedQuad> {
      return emptyList()
    }

    override fun getSprite(): Sprite {
      return wrapped.sprite
    }

    override fun useAmbientOcclusion(): Boolean {
      return wrapped.useAmbientOcclusion()
    }

    override fun hasDepth(): Boolean {
      return wrapped.hasDepth()
    }

    override fun getTransformation(): ModelTransformation {
      return wrapped.transformation
    }

    override fun isSideLit(): Boolean {
      return wrapped.isSideLit
    }

    override fun isBuiltin(): Boolean {
      return wrapped.isBuiltin
    }

    companion object {
      // copied from BakedQuadFactory

      val lightmapMap = Direction.values().asIterable().associateWith(::getLightmapCoordinate).let(::EnumMap)

      private fun getLightmapCoordinate(direction: Direction): Int {
        val f = getRelativeDirectionalBrightness(direction)
        val i = MathHelper.clamp((f * 255.0f).toInt(), 0, 255)
        return -16777216 or (i shl 16) or (i shl 8) or i
      }

      private fun getRelativeDirectionalBrightness(direction: Direction): Float {
        return when (direction) {
          DOWN -> 0.5f
          UP -> 1.0f
          NORTH, SOUTH -> 0.8f
          WEST, EAST -> 0.6f
          else -> 1.0f
        }
      }

    }

  }

}