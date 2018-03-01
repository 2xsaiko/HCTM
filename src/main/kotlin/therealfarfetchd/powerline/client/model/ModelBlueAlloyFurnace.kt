package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.BlockPowered
import therealfarfetchd.powerline.common.block.BlueAlloyFurnace
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.client.api.model.SimpleModel
import therealfarfetchd.quacklib.common.api.extensions.get

object ModelBlueAlloyFurnace : SimpleModel(), IIconRegister {
  lateinit var textureTop: TextureAtlasSprite
  lateinit var textureSide: TextureAtlasSprite
  lateinit var textureFront: TextureAtlasSprite
  lateinit var textureFrontPowered: TextureAtlasSprite
  lateinit var textureFrontLit: TextureAtlasSprite
  lateinit var textureBottom: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = textureTop

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapesForState(state[BlueAlloyFurnace.PropFacing], state[BlockPowered.PropPowered], state[BlueAlloyFurnace.PropLit], model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapesForState(EnumFacing.NORTH, false, false, model)
  }

  private fun addShapesForState(facing: EnumFacing, power: Boolean, burning: Boolean, model: ModelBuilder) = model {
    box {
      transform = "°Y${facing.horizontalAngle.toInt().toString().padStart(3, '0')}"

      down = texture(textureBottom)
      up = texture(textureTop)
      north = texture(textureSide)
      west = texture(textureSide)
      east = texture(textureSide)

      south = texture(
        if (power)
          if (burning) textureFrontLit
          else textureFrontPowered
        else textureFront
      )
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    with(textureMap) {
      textureTop = registerSprite(ResourceLocation(ModID, "blocks/blue_alloy_furnace_top"))
      textureSide = registerSprite(ResourceLocation(ModID, "blocks/blue_alloy_furnace_side"))
      textureFront = registerSprite(ResourceLocation(ModID, "blocks/blue_alloy_furnace_front"))
      textureFrontPowered = registerSprite(ResourceLocation(ModID, "blocks/blue_alloy_furnace_powered"))
      textureFrontLit = registerSprite(ResourceLocation(ModID, "blocks/blue_alloy_furnace_on"))
      textureBottom = registerSprite(ResourceLocation(ModID, "blocks/blue_alloy_furnace_bottom"))
    }
  }
}