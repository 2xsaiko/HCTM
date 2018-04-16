package therealfarfetchd.retrocomputers.client.render.component

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import therealfarfetchd.quacklib.common.api.util.math.Mat4
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.client.api.component.ComponentRender
import therealfarfetchd.retrocomputers.client.objloader.OBJGLRenderer
import therealfarfetchd.retrocomputers.client.objloader.loadOBJ
import therealfarfetchd.retrocomputers.client.objloader.orEmpty
import therealfarfetchd.retrocomputers.common.component.ComponentCPU

object RenderCPU : ComponentRender<ComponentCPU> {
  val tex = ResourceLocation(ModID, "textures/components/cpu_interface.png")
  val obj = OBJGLRenderer(loadOBJ(ResourceLocation(ModID, "models/component/cpu/cpu.obj")).orEmpty(),
    mapOf(
      "Material" to tex.toString(),
      "None" to tex.toString()
    ))

  val lightX = 960 / 1024.0
  val lightY = 160 / 1024.0
  val lightD = 13 / 1024.0

  override fun render(component: ComponentCPU, partialTicks: Float) {
    pushMatrix()
    color(1f, 1f, 1f, 1f)
    translate(0.5f, 0f, 0.5f)
    rotate(-90f, 0f, 1f, 0f)
    translate(-0.5f, 0f, -0.5f)
    obj.reset()

    val rotCenter = Vec3(0.97f, 0.12f, 0f)
    val switchMat = Mat4.Identity
      .translate(rotCenter)
      .rotate(0f, 0f, 1f, 55f)
      .translate(-rotCenter)

    val buttonMat = Mat4.translateMat(-0.0025f, 0f, 0f)

    for (i in 0 until 25) {
      if (getFlippedState(component, i))
        obj.transformPart("switch.${i.toString().padStart(3, '0')}", switchMat)
    }

    for (i in 0 until 6) {
      if (component.outputSelect == i)
        obj.transformPart("button.${i.toString().padStart(3, '0')}", buttonMat)
    }

    obj.draw()
    popMatrix()

    disableLighting()
    setLightmapDisabled(true)

    if (component.running) drawLight(617, 187, 0.95f)
    drawBits(129, 187, component.prevMemAddress, component.memAddress, partialTicks)
    drawBits(129, 227, component.prevOutputRegister, component.outputRegister, partialTicks)

    enableLighting()
    setLightmapDisabled(false)
  }

  private fun getFlippedState(component: ComponentCPU, i: Int): Boolean {
    return when (i) {
      0             -> component.powerOn
      in 1 until 17 -> component.inputRegister.toInt() and (1 shl (i - 1)) != 0
      17            -> component.btnAddrLoad > 0
      18            -> component.btnClear > 0
      19            -> component.btnContinue > 0
      20            -> component.btnExamine > 0
      21            -> component.btnHalt > 0
      22            -> component.btnSingleStep > 0
      23            -> component.btnDep == 0
      24            -> component.btnExtdDep == 0
      else          -> false
    }
  }

  private fun drawBits(x: Int, y: Int, prev: FloatArray, s: FloatArray, partialTicks: Float) {
    for (i in 0 until 16) {
      val r = if (prev === s) s[i] else {
        // prev[i] + (s[i] - prev[i]) * partialTicks
        (prev[i] + s[i]) / 2
      }
      drawLight(x + 29 * i, y, 0.65f * r)
    }
  }

  private fun drawLight(x: Int, y: Int, brightness: Float) {
    val pxSize = 0.75 / 960
    fun x(x: Int) = 2 / 16f + pxSize * x
    fun y(y: Int) = 1 / 16f + 0.375 - pxSize * y

    Minecraft.getMinecraft().textureManager.bindTexture(tex)

    val baseColor = Vec3(1f, 0.78f, 0.0f)

    color(baseColor.xf, baseColor.yf * brightness * 0.8f + baseColor.yf * 0.2f, baseColor.zf, brightness)

    val t = Tessellator.getInstance()
    val buf = t.buffer
    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
    val d = 1 - 0.375 / 16f
    buf.pos(x(x), y(y), d)
      .tex(lightX, lightY)
      .endVertex()
    buf.pos(x(x), y(y + 13), d)
      .tex(lightX, lightY + lightD)
      .endVertex()
    buf.pos(x(x + 13), y(y + 13), d)
      .tex(lightX + lightD, lightY + lightD)
      .endVertex()
    buf.pos(x(x + 13), y(y), d)
      .tex(lightX + lightD, lightY)
      .endVertex()
    t.draw()
  }

  private fun setLightmapDisabled(disabled: Boolean) {
    setActiveTexture(OpenGlHelper.lightmapTexUnit)

    if (disabled) {
      disableTexture2D()
    } else {
      enableTexture2D()
    }

    setActiveTexture(OpenGlHelper.defaultTexUnit)
  }
}