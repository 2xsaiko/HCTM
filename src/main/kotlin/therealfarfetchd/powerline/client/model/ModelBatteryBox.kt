package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.BatteryBox
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.client.api.model.SimpleModel
import therealfarfetchd.quacklib.common.api.extensions.get

object ModelBatteryBox : SimpleModel(), IIconRegister {
  lateinit var textureTop: TextureAtlasSprite
  lateinit var textureSideEmpty: TextureAtlasSprite
  lateinit var textureSideFull: TextureAtlasSprite
  lateinit var textureBottom: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = textureSideEmpty

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapesForLevel(state[BatteryBox.PropCharge], model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapesForLevel(BatteryBox.getChargeForDisplay(stack.metadata), model)
  }

  private fun addShapesForLevel(powerLevel: Int, model: ModelBuilder) = model {
    val l = 3 + powerLevel

    box {
      max = vec16(16, l, 16)

      down = texture(textureBottom)
      val side = texture(textureSideFull)
      north = side
      south = side
      west = side
      east = side
    }

    box {
      min = vec16(0, l, 0)

      up = texture(textureTop)
      val side = texture(textureSideEmpty)
      north = side
      south = side
      west = side
      east = side
    }
  }

  override fun createKey(stack: ItemStack, face: EnumFacing?): String =
    "$face@${stack.item.registryName}:${BatteryBox.getChargeForDisplay(stack.metadata)}@"

  override fun registerIcons(textureMap: TextureMap) {
    with(textureMap) {
      textureTop = registerSprite(ResourceLocation(ModID, "blocks/batterybox_top"))
      textureSideEmpty = registerSprite(ResourceLocation(ModID, "blocks/batterybox_s_empty"))
      textureSideFull = registerSprite(ResourceLocation(ModID, "blocks/batterybox_s_full"))
      textureBottom = registerSprite(ResourceLocation(ModID, "blocks/batterybox_bottom"))
    }
  }
}