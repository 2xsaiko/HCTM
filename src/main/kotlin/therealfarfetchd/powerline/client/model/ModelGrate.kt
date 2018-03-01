package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.Grate
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.client.api.model.SimpleModel
import therealfarfetchd.quacklib.common.api.extensions.get

object ModelGrate : SimpleModel(), IIconRegister {

  lateinit var textureSide: TextureAtlasSprite
  lateinit var textureFront: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = textureSide

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapes(state[Grate.PropFacing], model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapes(UP, model)
  }

  private fun addShapes(facing: EnumFacing, model: ModelBuilder) = model {
    box {
      val tex = texture(textureSide)
      val texf = texture(textureFront)
      down = if (facing == DOWN) texf else tex
      up = if (facing == UP) texf else tex
      north = if (facing == NORTH) texf else tex
      south = if (facing == SOUTH) texf else tex
      west = if (facing == WEST) texf else tex
      east = if (facing == EAST) texf else tex
    }

    box {
      min = vec16(0.1, 0.1, 0.1)
      max = vec16(15.9, 15.9, 15.9)

      inverted = true

      val tex = texture(textureSide)
      val texf = texture(textureFront)
      down = if (facing == DOWN) texf else tex
      up = if (facing == UP) texf else tex
      north = if (facing == NORTH) texf else tex
      south = if (facing == SOUTH) texf else tex
      west = if (facing == WEST) texf else tex
      east = if (facing == EAST) texf else tex
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    textureSide = textureMap.registerSprite(ResourceLocation(ModID, "blocks/grate_side"))
    textureFront = textureMap.registerSprite(ResourceLocation(ModID, "blocks/grate_front"))
  }
}