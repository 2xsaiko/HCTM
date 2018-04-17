package therealfarfetchd.retrocomputers.client.objloader

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import therealfarfetchd.quacklib.common.api.util.math.Mat4
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.quacklib.common.api.util.math.glMultMatrix
import therealfarfetchd.quacklib.common.api.util.math.times

@SideOnly(Side.CLIENT)
class OBJGLRenderer(val obj: OBJRoot, val textureOverrides: Map<String, String> = emptyMap()) {
  private var translations: Map<Object, Mat4> = emptyMap()

  private val t = Tessellator.getInstance()
  private val buf = t.buffer

  private val tm = Minecraft.getMinecraft().textureManager

  private var isDirty = true

  fun reset() {
    translations = emptyMap()
    isDirty = true
  }

  fun transformPart(name: String, mat: Mat4) {
    val part = obj.objects[name] ?: return
    val ct = translations[part] ?: Mat4.Identity
    translations += part to (ct * mat)
    isDirty = true
  }

  fun transformGroup(name: String, mat: Mat4) {
    obj.objects.filter { (_, part) -> name in part.groups }.keys.forEach { transformPart(it, mat) }
    isDirty = true
  }

  private var currentTexture: ResourceLocation? = null

  fun draw() {
    bindTexture(0)
    obj.faces.forEach(::drawFace)
    for (o in obj.objects.values) {
      pushMatrix()
      translations[o]?.also { glMultMatrix(it) }
      o.faces.forEach(::drawFace)
      popMatrix()
    }
    currentTexture = null
    isDirty = false
  }

  private fun drawFace(face: Face) {
    val mat = obj.materials[face.material] ?: fallbackMaterial
    val rl = (textureOverrides[face.material] ?: mat.diffuseTexture)?.let(::ResourceLocation)
    if (rl != currentTexture) {
      if (rl == null) bindTexture(0)
      else tm.bindTexture(rl)
      currentTexture = rl
    }
    buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR)
    for (vertex in face.vertices) {
      val (x, y, z) = obj.vertPos[getRealIndex(obj.vertPos.size, vertex.xyz)]
      val (u, v) = if (vertex.tex != null) obj.vertTex[getRealIndex(obj.vertTex.size, vertex.tex)] else Vec3(0, 0, 0)
      val (r, g, b) = mat.diffuse.getRGBColorComponents(FloatArray(3))
      buf.pos(x, y, z)
        .tex(u, 1 - v)
        .color(r, g, b, mat.transparency)
        .endVertex()
    }
    t.draw()
  }

  private fun getRealIndex(total: Int, a: Int) = when {
    a > 0 -> a - 1
    a < 0 -> total + a
    else  -> error("Invalid index!")
  }
}