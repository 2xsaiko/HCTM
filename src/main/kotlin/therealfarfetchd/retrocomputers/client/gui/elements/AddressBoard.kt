package therealfarfetchd.retrocomputers.client.gui.elements

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui.drawScaledCustomSizeModalRect
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.init.SoundEvents
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.gui.GuiElement
import therealfarfetchd.quacklib.client.api.gui.number
import therealfarfetchd.quacklib.common.api.extensions.flip
import therealfarfetchd.quacklib.common.api.extensions.packByte
import therealfarfetchd.quacklib.common.api.extensions.unpack
import therealfarfetchd.retrocomputers.ModID

class AddressBoard : GuiElement() {
  val texture = ResourceLocation(ModID, "textures/gui/guiex.png")

  var value: Byte by number()

  init {
    width = 89
    height = 31

    value = 0
  }

  override fun render(mouseX: Int, mouseY: Int) {
    mc.textureManager.bindTexture(texture)
    enableTexture2D()
    color(1F, 1F, 1F)
    drawScaledCustomSizeModalRect(0, 0, 0F, 19F, 89, 31, 89, 31, 128F, 128F)
    pushMatrix()
    translate(6.0, 5.0, 0.0)
    for (i in 0..7) {
      if (unpack(value)[i]) drawScaledCustomSizeModalRect(0, 0, 22F, 5F, 7, 14, 7, 14, 128F, 128F)
      translate(10F, 0F, 0F)
    }
    popMatrix()
  }

  override fun mouseClicked(x: Int, y: Int, button: Int) {
    super.mouseClicked(x, y, button)
    var xrel = x - 6
    val yrel = y - 5
    for (i in 0..7) {
      if (xrel in 0..7 && yrel in 0..14) {
        val bits = unpack(value)
        mc.soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, if (bits[i]) 1.5F else 2F))
        bits.flip(i)
        value = packByte(*bits)
        fireEvent()
        break
      }
      xrel -= 10
    }
  }


}