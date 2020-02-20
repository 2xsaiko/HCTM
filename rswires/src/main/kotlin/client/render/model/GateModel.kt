package net.dblsaiko.rswires.client.render.model

import net.dblsaiko.rswires.common.block.GateProperties
import net.dblsaiko.rswires.common.util.getRotationFor
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import therealfarfetchd.qcommon.croco.Vec3
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class GateModel(val wrapped: UnbakedModel) : UnbakedModel by wrapped {

  val map = IdentityHashMap<BakedModel, Baked>()

  override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel? {
    return map.computeIfAbsent(wrapped.bake(loader, textureGetter, rotationContainer, modelId) ?: return null, ::Baked)
  }

  class Baked(val wrapped: BakedModel) : FabricBakedModel, BakedModel by wrapped {

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
      val side = state[Properties.FACING]
      val rotation = state[GateProperties.ROTATION]
      val (mat, rotationMat) = getRotationFor(side, rotation)

      context.pushTransform { quad ->
        val lightFace = quad.lightFace()
        run {
          val shadingFactor = ColorHelper.diffuseShade(lightFace)
          for (idx in 0..3) {
            val color = ColorHelper.multiplyRGB(quad.spriteColor(idx, 0), (1 / shadingFactor))
            quad.spriteColor(idx, 0, color)
          }
        }

        for (idx in 0..3) {
          val newPos = mat.mul(Vec3(quad.posByIndex(idx, 0), quad.posByIndex(idx, 1), quad.posByIndex(idx, 2)))
          quad.pos(idx, newPos.x, newPos.y, newPos.z)
        }

        run {
          val lightFace = rotationMat.mul(Vec3.from(lightFace.vector)).let { Direction.getFacing(it.x, it.y, it.z) }
          val shadingFactor = ColorHelper.diffuseShade(lightFace)
          for (idx in 0..3) {
            val color = ColorHelper.multiplyRGB(quad.spriteColor(idx, 0), shadingFactor)
            quad.spriteColor(idx, 0, color)
          }
        }

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

    override fun getQuads(state: BlockState?, face: Direction?, random: Random): List<BakedQuad> {
      return emptyList()
    }

  }

}