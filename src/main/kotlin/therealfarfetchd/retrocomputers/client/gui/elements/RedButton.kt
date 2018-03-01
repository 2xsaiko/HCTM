package therealfarfetchd.retrocomputers.client.gui.elements

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.init.SoundEvents
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.gui.ButtonType
import therealfarfetchd.quacklib.client.api.gui.elements.Button
import therealfarfetchd.retrocomputers.ModID

class RedButton : Button() {
  private val btnTexture = ResourceLocation(ModID, "textures/gui/guiex.png")

  init {
    width = 11
    height = 11
  }

  override fun render(mouseX: Int, mouseY: Int) {
    mc.textureManager.bindTexture(btnTexture)
    enableTexture2D()
    color(1F, 1F, 1F)
    enableBlend()
    Gui.drawScaledCustomSizeModalRect(0, 0, if ((variant == ButtonType.Toggle && toggled) || clicked) 11F else 0F, 8F, 11, 11, 11, 11, 128F, 128F)
    disableBlend()
  }

  override fun buttonClick(button: Int) {
    val pitch = if (variant == ButtonType.Toggle) if (toggled) 2.0f else 1.5f else 1.5f
    mc.soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, pitch))
    fireEvent()
  }
}