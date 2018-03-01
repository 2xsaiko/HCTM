package therealfarfetchd.rswires.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.client.api.model.SimpleModel
import therealfarfetchd.quacklib.common.api.extensions.RGBA
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.quacklib.common.api.extensions.mapWithCopy
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.block.LampOn
import therealfarfetchd.rswires.common.block.LampProperties

object ModelLamp : SimpleModel(), IIconRegister {
  override lateinit var particleTexture: TextureAtlasSprite

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    val renderHalo = MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT
    addShapes(model, state.block.registryName == LampOn.registryName, state[LampProperties.PropColor], renderHalo)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    val on = stack.item.registryName == LampOn.registryName
    val color = EnumDyeColor.byMetadata(stack.metadata)
    addShapes(model, on, color, false)
  }

  private fun addShapes(model: ModelBuilder, on: Boolean, color: EnumDyeColor, renderHalo: Boolean) = model {
    if (!renderHalo)
      box {
        val tex = textures[on to color]!!
        particleTexture = tex
        all = texture(tex)
      }
    else
      box {
        min = vec16(-1, -1, -1)
        max = vec16(17, 17, 17)

        val (r, g, b) = color.colorComponentValues
        val brightness = MathHelper.sqrt((r * r + g * g + b * b) / 3f)

        // TODO: more straight forward way of setting the color
        all = texture(haloTexture, 0, 0, 1, 1)
          .copy(postProc = { copy(color = RGBA(r, g, b, 0.05f + brightness * 0.45f)) })
      }
  }

  override fun registerIcons(textureMap: TextureMap) {
    textures = texturePaths.mapValues { textureMap.registerSprite(it.value) }
    haloTexture = textureMap.registerSprite(ResourceLocation(ModID, "blocks/lamp/halo"))
  }

  override fun createKey(state: IExtendedBlockState, face: EnumFacing?) =
    super.createKey(state, face) + MinecraftForgeClient.getRenderLayer()

  private lateinit var textures: Map<Pair<Boolean, EnumDyeColor>, TextureAtlasSprite>

  private lateinit var haloTexture: TextureAtlasSprite

  private val texturePaths = setOf(true, false)
    .flatMap { b -> EnumDyeColor.values().map { b to it } }
    .mapWithCopy { (on, color) -> ResourceLocation(ModID, "blocks/lamp/${if (on) "on" else "off"}/${color.dyeColorName}") }
    .toMap()
}