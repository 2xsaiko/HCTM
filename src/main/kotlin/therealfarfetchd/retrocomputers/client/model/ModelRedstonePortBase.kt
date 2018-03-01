package therealfarfetchd.retrocomputers.client.model

import net.minecraft.block.BlockHorizontal
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.client.api.model.SimpleModel
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.quacklib.common.api.extensions.unsigned
import therealfarfetchd.retrocomputers.ModID

abstract class ModelRedstonePortBase : SimpleModel(), IIconRegister {

  lateinit var textureTop: TextureAtlasSprite
  lateinit var textureBottom: TextureAtlasSprite
  lateinit var textureSide: TextureAtlasSprite
  lateinit var textureBack: TextureAtlasSprite
  lateinit var textureFront: TextureAtlasSprite
  lateinit var textureDisplay: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = textureTop

  abstract val texturePrefix: String

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapes(isTop(state), getDisplay(state), state[BlockHorizontal.FACING], model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapes(false, 0, EnumFacing.NORTH, model)
  }

  fun addShapes(top: Boolean, display: Short, rot: EnumFacing, model: ModelBuilder) = model {
    val tr = "°Y${rot.horizontalAngle.toInt().toString().padStart(3, '…')}"

    box {
      transform = tr

      if (top) min = vec16(0, 8, 0)
      if (!top) max = vec16(16, 8, 16)

      down = texture(textureBottom)
      up = texture(textureTop)
      north = texture(textureBack)
      south = texture(textureFront)
      west = texture(textureSide)
      east = texture(textureSide)
    }

    for (i in 0..3) {
      val offsetY = if (top) 12 else 4 - if (i > 1) 1 else 0
      val offsetX = if (i % 2 == 0) 4 else 8
      val sprid = display.unsigned shr (i * 4) and 15

      box {
        transform = tr

        min = vec16(offsetX, offsetY, 0)
        max = vec16(offsetX + 4, offsetY + 1, 16)
        south = texture16(textureDisplay, 0, sprid, 4, sprid + 1)
      }
    }
  }

  abstract fun isTop(state: IExtendedBlockState): Boolean

  abstract fun getDisplay(state: IExtendedBlockState): Short

  override fun createKey(state: IExtendedBlockState, face: EnumFacing?): String {
    return super.createKey(state, face) + getDisplay(state)
  }

  override fun registerIcons(textureMap: TextureMap) {
    with(textureMap) {
      textureTop = registerSprite(ResourceLocation(ModID, "blocks/${texturePrefix}_top"))
      textureBottom = registerSprite(ResourceLocation(ModID, "blocks/${texturePrefix}_bottom"))
      textureSide = registerSprite(ResourceLocation(ModID, "blocks/${texturePrefix}_side"))
      textureBack = registerSprite(ResourceLocation(ModID, "blocks/${texturePrefix}_back"))
      textureFront = registerSprite(ResourceLocation(ModID, "blocks/${texturePrefix}_front"))
      textureDisplay = registerSprite(ResourceLocation(ModID, "blocks/rs_display"))
    }
  }

}