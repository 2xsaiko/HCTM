package therealfarfetchd.retrocomputers.client.render.block

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11
import therealfarfetchd.retrocomputers.client.api.component.ComponentRender
import therealfarfetchd.retrocomputers.common.api.component.Component
import therealfarfetchd.retrocomputers.common.block.Rack

object RackRenderer : TileEntitySpecialRenderer<Rack.Tile>() {
  override fun render(te: Rack.Tile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
    RenderHelper.disableStandardItemLighting()
    blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    enableBlend()
    enableLighting()

    if (Minecraft.isAmbientOcclusionEnabled()) {
      shadeModel(GL11.GL_SMOOTH)
    } else {
      shadeModel(GL11.GL_FLAT)
    }

    pushMatrix()
    translate(x + 0.5, y, z + 0.5)
    rotate(180 - EnumFacing.HORIZONTALS[te.blockMetadata].horizontalAngle, 0f, 1f, 0f)
    translate(-0.5, 0.0, -0.5)
    for (i in 0 until te.slotCount) {
      pushMatrix()
      val component = te.container.getComponent(i)
      @Suppress("UNCHECKED_CAST")
      val renderer = component.getRenderer() as ComponentRender<Component>
      renderer.render(component, partialTicks)
      popMatrix()
      translate(0f, 7 / 16f, 0f)
    }
    popMatrix()

    RenderHelper.enableStandardItemLighting()
  }
}