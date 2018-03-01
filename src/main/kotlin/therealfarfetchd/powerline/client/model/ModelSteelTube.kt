/*
 * Copyright (c) 2017 Marco Rebhan (the_real_farfetchd)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package therealfarfetchd.powerline.client.model

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.powerline.ModID
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.client.api.model.SimpleModel

object ModelSteelTube : SimpleModel(), IIconRegister {
  lateinit var texture: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = error("Wat? This is an item, you doofus.")

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    error("Wat? This is an item, you doofus.")
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) = model {
    translate(center)
    rotate(1f, 0f, 0f, 90f)
    translate(-center)

    cyl {
      side = texture(64, texture, 0, 0, 48, 16)
      up = texture(missingTex)
      down = texture(missingTex)
    }

    cyl {
      min = vec16(0.01f, 0f, 0.01f)
      max = vec16(15.99f, 16f, 15.99f)
      inverted = true

      side = texture(64, texture, 0, 0, 48, 16)
      up = texture(missingTex)
      down = texture(missingTex)
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    texture = textureMap.registerSprite(ResourceLocation(ModID, "items/steeltube_l"))
  }
}

object ModelSteelTubeSmall : SimpleModel(), IIconRegister {
  lateinit var texture: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = error("Wat? This is an item, you doofus.")

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    error("Wat? This is an item, you doofus.")
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) = model {
    translate(center)
    rotate(1f, 0f, 0f, 90f)
    translate(-center)

    cyl {
      res = 45

      min = vec16(4, 0, 4)
      max = vec16(12, 16, 12)

      side = texture(64, texture, 0, 0, 32, 16)
      up = texture(missingTex)
      down = texture(missingTex)
    }

    cyl {
      res = 45

      min = vec16(4.01f, 0f, 4.01f)
      max = vec16(11.99f, 16f, 11.99f)
      inverted = true

      side = texture(64, texture, 0, 0, 32, 16)
      up = texture(missingTex)
      down = texture(missingTex)
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    texture = textureMap.registerSprite(ResourceLocation(ModID, "items/steeltube_l"))
  }
}