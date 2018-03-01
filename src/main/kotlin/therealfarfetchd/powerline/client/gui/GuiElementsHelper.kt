package therealfarfetchd.powerline.client.gui

import net.minecraft.client.gui.Gui
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.quacklib.client.gui.bindTexture
import therealfarfetchd.quacklib.client.gui.getScaled

val texture = ResourceLocation(ModID, "textures/gui/power_elements.png")

fun Gui.drawPowerBar(x: Int, y: Int, capacity: Int) {
  bindTexture(texture)
  val heightT = getScaled(100, capacity, 33)
  drawTexturedModalRect(x, y, 0, 0, 7, 33)
  if (heightT > 0) drawTexturedModalRect(x, y + 33 - heightT, 7, 33 - heightT, 7, heightT)
}

fun Gui.drawBigPowerBar(x: Int, y: Int, capacity: Int) {
  bindTexture(texture) // 18x49, 14 0, 32 0
  val heightT = getScaled(100, capacity, 49)
  drawTexturedModalRect(x, y, 14, 0, 18, 49)
  if (heightT > 0) drawTexturedModalRect(x, y + 49 - heightT, 32, 49 - heightT, 18, heightT)
}

fun Gui.drawBigPowerBarWithIcon(x: Int, y: Int, capacity: Int) {
  drawBigPowerBar(x, 8 + y, capacity)
  drawPowerIcon(6 + x, y, PowerIcon.BigBattery, capacity > 0)
}

fun Gui.drawPowerIcon(x: Int, y: Int, icon: PowerIcon, status: Boolean) {
  bindTexture(texture)
  val tx = if (status) icon.onX else icon.offX
  val ty = if (status) icon.onY else icon.offY
  val tw = if (status) icon.onWidth else icon.offWidth
  val th = if (status) icon.onHeight else icon.offHeight
  drawTexturedModalRect(x, y, tx, ty, tw, th)
}

fun Gui.drawPowerBarWithIcon(x: Int, y: Int, voltageLevel: Int, vThreshold: Int) {
  val vOn = voltageLevel >= vThreshold
  drawPowerBar(x, 8 + y, voltageLevel)
  drawPowerIcon(2 + x, 1 + y, PowerIcon.Battery, vOn)
}

fun Gui.drawChargeBars(x: Int, y: Int, voltageLevel: Int, chargeLevel: Int, vThreshold: Int) {
  val cOn = chargeLevel >= 100
  drawPowerBarWithIcon(x, y, voltageLevel, vThreshold)
  drawPowerBar(8 + x, 8 + y, chargeLevel)
  drawPowerIcon(10 + x, 1 + y, PowerIcon.Power, cOn)
}

enum class PowerIcon(
  val offX: Int,
  val offY: Int,
  val offWidth: Int,
  val offHeight: Int,
  val onX: Int,
  val onY: Int,
  val onWidth: Int = offWidth,
  val onHeight: Int = offHeight + 1
) {
  Battery(0, 33, 3, 5, 0, 38),
  Power(3, 33, 3, 5, 3, 38),
  BigBattery(6, 33, 6, 5, 6, 38)
}