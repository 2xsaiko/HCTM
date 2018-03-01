package therealfarfetchd.retrocomputers.client.gui.elements

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.init.SoundEvents
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.gui.ButtonType
import therealfarfetchd.quacklib.client.api.gui.elements.Button
import therealfarfetchd.retrocomputers.ModID

class FlipSwitch : Button() {
  private val btnTexture = ResourceLocation(ModID, "textures/gui/guiex.png")

  init {
    width = 18
    height = 24
  }

  override fun render(mouseX: Int, mouseY: Int) {
    mc.textureManager.bindTexture(btnTexture)
    enableTexture2D()
    color(1F, 1F, 1F)
    enableBlend()
    Gui.drawScaledCustomSizeModalRect(0, 0, if ((variant == ButtonType.Toggle && toggled) || clicked) 18F else 0F, 50F, 18, 24, 18, 24, 128F, 128F)
    disableBlend()
  }

  override fun buttonClick(button: Int) {
    val pitch = if (variant == ButtonType.Toggle) if (toggled) 1.25F else 0.75f else 0.75f
    mc.soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, pitch))
    fireEvent()
  }
}