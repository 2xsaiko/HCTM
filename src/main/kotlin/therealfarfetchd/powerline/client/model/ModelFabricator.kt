package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.BlockPowered
import therealfarfetchd.powerline.common.block.Fabricator
import therealfarfetchd.quacklib.client.api.model.DynamicSimpleModel
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.common.api.extensions.get

object ModelFabricator : DynamicSimpleModel<Fabricator>(), IIconRegister {

  lateinit var texture: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = texture


  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapesForState(state[BlockPowered.PropPowered], state[Fabricator.PropActive], model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapesForState(false, false, model)
  }

  fun addShapesForState(power: Boolean, active: Boolean, model: ModelBuilder) = model {
    box {
      up = texture(64, texture, 0, 0, 16, 16)
      down = texture(64, texture, 32, 0, 48, 16)

      val side = texture(64, texture, 0, 16, 16, 32)
      val side1 = texture(64, texture, 16, 16, 0, 32)
      north = side1
      south = side
      west = side
      east = side1
    }

    box {
      max = vec16(16, 8, 16)

      up = texture(64, texture, 48, 0, 64, 16)
    }

    box {
      min = vec16(0, 11, 0)

      down = texture(64, texture, 32, 16, 48, 32)
    }

    box {
      min = vec16(2, 8, 0)
      max = vec16(14, 11, 16)

      inverted = true

      west = texture(64, texture, 16, 24, 32, 27)
      east = texture(64, texture, 16, 24, 32, 27)
    }

    box {
      min = vec16(0, 8, 2)
      max = vec16(16, 11, 14)

      inverted = true

      north = texture(64, texture, 16, 24, 32, 27)
      south = texture(64, texture, 16, 24, 32, 27)
    }

    val activeT = if (active) texture(64, texture, 18, 16, 20, 19) else texture(64, texture, 16, 16, 18, 19)
    val powerT = if (power) texture(64, texture, 18, 19, 20, 21) else texture(64, texture, 16, 19, 18, 21)

    for (i in 0..3) {
      val rot = "°Y${(90 * i).toString().padStart(3, '0')}"

      box {
        min = vec16(12, 4, 0)
        max = vec16(14, 7, 0)

        transform = rot

        north = activeT
      }

      box {
        min = vec16(12, 2, 0)
        max = vec16(14, 4, 0)

        transform = rot

        north = powerT
      }
    }
  }

  override fun addShapesDynamic(block: Fabricator, model: ModelBuilder) {

  }

  override fun registerIcons(textureMap: TextureMap) {
    texture = textureMap.registerSprite(ResourceLocation(ModID, "blocks/fabricator"))
  }
}