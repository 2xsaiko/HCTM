package therealfarfetchd.retrocomputers.client.gui.elements

import net.minecraft.client.gui.Gui.drawScaledCustomSizeModalRect
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.gui.GuiElement
import therealfarfetchd.quacklib.client.api.gui.mapper
import therealfarfetchd.quacklib.client.api.gui.number
import therealfarfetchd.retrocomputers.ModID

class Light : GuiElement() {
  val texture = ResourceLocation(ModID, "textures/gui/guiex.png")

  var value: Boolean by mapper()
  var r: Float by number()
  var g: Float by number()
  var b: Float by number()

  init {
    width = 12
    height = 7
    r = 0.75f
    g = 0f
    b = 0f

    value = false
  }

  override fun render(mouseX: Int, mouseY: Int) {
    mc.textureManager.bindTexture(texture)
    enableTexture2D()
    enableBlend()
    color(1F, 1F, 1F)
    drawScaledCustomSizeModalRect(0, 0, 29F, 12F, 12, 7, 12, 7, 128F, 128F)
    color(r, g, b)
    if (value) {
      drawScaledCustomSizeModalRect(0, 0, 29F, 0F, 12, 7, 12, 7, 128F, 128F)
    } else {
      drawScaledCustomSizeModalRect(3, 1, 29F, 7F, 8, 5, 8, 5, 128F, 128F)
    }
    disableBlend()
    color(1F, 1F, 1F)
  }

}