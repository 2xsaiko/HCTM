package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.BlockPowered
import therealfarfetchd.powerline.common.block.PlayerLink
import therealfarfetchd.quacklib.client.api.model.DynamicSimpleModel
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.common.api.extensions.get

object ModelPlayerLink : DynamicSimpleModel<PlayerLink>(), IIconRegister {
  lateinit var texture: TextureAtlasSprite
  lateinit var textureTranslucent: TextureAtlasSprite
  lateinit var textureLightning: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = texture

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapes(model, state[BlockPowered.PropPowered], MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapes(model, false, false)
    addShapes(model, false, true)
  }

  private fun addShapes(model: ModelBuilder, isOn: Boolean, translucent: Boolean) = model {
    val texture = if (translucent) textureTranslucent else texture

    box {
      if (!translucent) {
        up = texture(64, texture, if (isOn) 32 else 16, 0, if (isOn) 48 else 32, 16)
        down = texture(64, texture, 16, 16, 32, 32)
      }
      sidesC = texture(64, texture, 0, 0, 16, 16)
    }

    box {
      min = vec16(1, 1, 1)
      max = vec16(15, 15, 15)
      inverted = true

      if (!translucent) {
        up = texture(64, texture, 0, 16, 14, 30)
        down = texture(64, texture, 0, 16, 14, 30)
      }
      sidesC = texture(64, texture, 48, 0, 62, 14)
    }

    if (!translucent) {
      for (i in 0..3) box {
        transformOp = { it.rotate(EnumFacing.Axis.Y, i * 90.0) }

        min = vec16(4, 4, 0)
        max = vec16(12, 12, 1)
        up = texture(64, texture, 32, 16, 33, 24, flip = true)
        down = texture(64, texture, 32, 16, 33, 24, flip = true)
        east = texture(64, texture, 32, 16, 33, 24)
        west = texture(64, texture, 32, 16, 33, 24)
        inverted = true
      }
    }
  }

  override fun addShapesDynamic(block: PlayerLink, model: ModelBuilder) = model {
    fullbright = true

    if (block.clientHasPower && block.isConnected) {
      sign {
        center = vec16(8, 8, 8)
        dimUp = 7f / 16f
        dimDown = 7f / 16f
        dimLeft = 7f / 16f
        dimRight = 7f / 16f

        tex = texture(textureLightning)
      }
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    texture = textureMap.registerSprite(ResourceLocation(ModID, "blocks/player_link"))
    textureTranslucent = textureMap.registerSprite(ResourceLocation(ModID, "blocks/player_link_translucent"))
    textureLightning = textureMap.registerSprite(ResourceLocation(ModID, "misc/lightning"))
  }

  override fun createKey(state: IExtendedBlockState, face: EnumFacing?) =
    super.createKey(state, face) + MinecraftForgeClient.getRenderLayer()
}