package therealfarfetchd.powerline.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.BlueAlloyFurnace
import therealfarfetchd.powerline.common.block.ContainerBlueAlloyFurnace
import therealfarfetchd.quacklib.client.gui.drawArrowRight
import therealfarfetchd.quacklib.client.gui.getScaled

class GuiBlueAlloyFurnace(val playerInv: InventoryPlayer, val inventory: BlueAlloyFurnace) : GuiContainer(ContainerBlueAlloyFurnace(playerInv, inventory)) {

  val container: ContainerBlueAlloyFurnace = inventorySlots as ContainerBlueAlloyFurnace

  val texture = ResourceLocation(ModID, "textures/gui/blue_alloy_furnace.png")

  override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    super.drawDefaultBackground()
    super.drawScreen(mouseX, mouseY, partialTicks)
    renderHoveredToolTip(mouseX, mouseY)
  }

  override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
    val s = inventory.displayName.unformattedText
    this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752)
    this.fontRenderer.drawString(playerInv.displayName.unformattedText, 8, this.ySize - 96 + 2, 4210752)
  }

  override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
    GlStateManager.color(1f, 1f, 1f)
    Minecraft.getMinecraft().textureManager.bindTexture(texture)
    val windowX = (this.width - this.xSize) / 2
    val windowY = (this.height - this.ySize) / 2

    drawTexturedModalRect(windowX, windowY, 0, 0, xSize, ySize)

    drawArrowRight(windowX + 102, windowY + 34, getScaled(container.totalCookTime, container.cookTime, 100))
    drawChargeBars(windowX + 5, windowY + 5, inventory.clientVLevel, inventory.clientCLevel, 60)

    GlStateManager.enableLighting()
  }

}