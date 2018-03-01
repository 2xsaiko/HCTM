package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.BlockPowered
import therealfarfetchd.powerline.common.block.Pump
import therealfarfetchd.quacklib.client.api.model.DynamicSimpleModel
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.common.api.extensions.get

object ModelPump : DynamicSimpleModel<Pump>(), IIconRegister {
  lateinit var texture: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = texture

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    addShapes(state[Pump.PropFacing], state[BlockPowered.PropPowered], state[Pump.PropActive], model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapes(EnumFacing.NORTH, false, false, model)

    model {
      box {
        min = vec16(3, 5, 7)
        max = vec16(13, 15, 10)

        val tex = texture(64, texture, 26, 21, 29, 31)
        val tex2 = texture(64, texture, 16, 21, 26, 31)
        down = tex.copy(flip = true)
        up = tex.copy(flip = true)
        north = tex2
        south = tex2
        west = tex
        east = tex
      }
    }
  }

  override fun addShapesDynamic(block: Pump, model: ModelBuilder) = model {
    val facing = block.facing

    val anim = -MathHelper.cos(block.animProgress * 6.28318531F)

    val offset = (anim + 1) * 2.5

    translate(center)
    when {
      facing.axis != EnumFacing.Axis.Y -> rotate(0f, 1f, 0f, facing.horizontalAngle)
      facing == EnumFacing.UP -> rotate(1f, 0f, 0f, 90f)
      else -> rotate(1f, 0f, 0f, -90f)
    }
    translate(-center)

    box {
      min = vec16(3.0, 5.0, 6 + offset)
      max = vec16(13.0, 15.0, 9 + offset)

      val tex = texture(64, texture, 26, 21, 29, 31)
      val tex2 = texture(64, texture, 16, 21, 26, 31)
      down = tex.copy(flip = true)
      up = tex.copy(flip = true)
      north = tex2
      south = tex2
      west = tex
      east = tex
    }
  }

  private fun addShapes(facing: EnumFacing, power: Boolean, active: Boolean, model: ModelBuilder) = model {
    translate(center)
    when {
      facing.axis != EnumFacing.Axis.Y -> rotate(0f, 1f, 0f, facing.horizontalAngle)
      facing == EnumFacing.UP -> rotate(1f, 0f, 0f, 90f)
      else -> rotate(1f, 0f, 0f, -90f)
    }
    translate(-center)

    box {
      down = texture(64, texture, 0, 0, 16, 16)
      up = texture(64, texture, 32, 16, 48, 32, flip = true)
      north = texture(64, texture, 0, 16, 16, 32)
      south = texture(64, texture, 32, 0, 48, 16)
      east = texture(64, texture, 16, 0, 32, 16)
      west = texture(64, texture, 16, 0, 32, 16)
    }

    box {
      min = vec16(0, 4, 6)
      max = vec16(16, 16, 14)

      inverted = true

      down = texture(64, texture, 38, 32, 46, 48, flip = true)
      north = texture(64, texture, 0, 32, 16, 44)
      south = texture(64, texture, 16, 32, 32, 44)
    }

    box {
      min = vec16(6, 8, 6)
      max = vec16(10, 12, 14)

      down = texture(64, texture, 16, 16, 24, 20, flip = true)
      up = texture(64, texture, 16, 16, 24, 20, flip = true)
      east = texture(64, texture, 16, 16, 24, 20)
      west = texture(64, texture, 16, 16, 24, 20)
    }

    box {
      val offset = if (active) 2 else 0

      min = vec16(0, 5, 2)
      max = vec16(16, 8, 4)

      val tex = texture(64, texture, 24 + offset, 16, 26 + offset, 19)
      east = tex
      west = tex
    }

    box {
      val offset = if (power) 2 else 0

      min = vec16(0, 3, 2)
      max = vec16(16, 5, 4)

      val tex = texture(64, texture, 24 + offset, 19, 26 + offset, 21)
      east = tex
      west = tex
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    texture = textureMap.registerSprite(ResourceLocation(ModID, "blocks/pump"))
  }
}