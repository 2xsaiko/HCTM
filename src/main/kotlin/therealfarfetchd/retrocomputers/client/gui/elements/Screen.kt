package therealfarfetchd.retrocomputers.client.gui.elements

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import therealfarfetchd.quacklib.client.api.gui.GuiElement
import therealfarfetchd.quacklib.client.api.gui.mapper
import therealfarfetchd.quacklib.client.api.gui.number
import therealfarfetchd.quacklib.common.api.extensions.shr
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import java.io.InputStream
import java.util.*
import kotlin.experimental.and

class Screen : GuiElement() {

  val tilesX: Int = 80
  val tilesY: Int = 50

  val charset: ByteArray = ByteArray(2048)
  val chars: ByteArray = ByteArray(tilesX * tilesY)
  var border_r: Float by number()
  var border_g: Float by number()
  var border_b: Float by number()
  var bg_r: Float by number()
  var bg_g: Float by number()
  var bg_b: Float by number()
  var px_r: Float by number()
  var px_g: Float by number()
  var px_b: Float by number()

  var crt: Boolean by mapper()

  val textureWidth = tilesX * 8
  val textureHeight = tilesY * 8

  val cscl = ChangeListener(this::charset)
  var first = true

  init {
    border_r = 0.2F
    border_g = 0.2F
    border_b = 0.2F
    bg_r = 0.0F
    bg_g = 0.0F
    bg_b = 0.0F
    px_r = 0.7F
    px_g = 0.7F
    px_b = 0.7F
    crt = true

    width = 8 + 4 * tilesX
    height = 8 + 4 * tilesY
  }

  override fun render(mouseX: Int, mouseY: Int) {
    val bgcol = -16777216 or ((bg_r * 256).toInt() shl 16) or ((bg_g * 256).toInt() shl 8) or (bg_b * 256).toInt()
    val bocol = -16777216 or ((border_r * 256).toInt() shl 16) or ((border_g * 256).toInt() shl 8) or (border_b * 256).toInt()

    GlStateManager.disableTexture2D()
    Gui.drawRect(1, 0, width - 1, height, bocol)
    Gui.drawRect(0, 1, width, height - 1, bocol)
    Gui.drawRect(3, 2, width - 3, height - 2, bgcol)
    Gui.drawRect(2, 3, width - 2, height - 3, bgcol)

    GlStateManager.scale(0.5, 0.5, 1.0)
    GlStateManager.translate(7.0, 8.0, 0.0)

    val tessellator = Tessellator.getInstance()
    val vertexbuffer = tessellator.buffer

    val fb = if (supportsShader && crt) setupFramebuffer() else null

    GlStateManager.enableTexture2D()
    val cs = charset
    if (cscl.valuesChanged() || first) {
      first = false
      val data = IntArray(16384)
      var i = 0
      for (f in cs) {
        for (b in 0..7) {
          data[i] = ((((f shr b) and 1) * 255) shl 24) or 16777215
          i += 1
        }
      }
      TextureUtil.uploadTexture(tilesetData, data, 8, 2048)
    } else GlStateManager.bindTexture(tilesetData)
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
    GlStateManager.color(px_r, px_g, px_b, 1.0f)
    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX)
    for (x in 0 until tilesX) for (y in 0 until tilesY) {
      val tileId = chars[x + y * tilesX]
      val posX = x * 8
      val posY = y * 8
      val vBegin = (tileId + 1) / 256f
      val vEnd = tileId / 256f
      vertexbuffer.pos(posX.toDouble(), (posY + 8).toDouble(), 0.0).tex(1.0, vBegin.toDouble()).endVertex()
      vertexbuffer.pos((posX + 8).toDouble(), (posY + 8).toDouble(), 0.0).tex(0.0, vBegin.toDouble()).endVertex()
      vertexbuffer.pos((posX + 8).toDouble(), posY.toDouble(), 0.0).tex(0.0, vEnd.toDouble()).endVertex()
      vertexbuffer.pos(posX.toDouble(), posY.toDouble(), 0.0).tex(1.0, vEnd.toDouble()).endVertex()
    }
    tessellator.draw()

    fb?.also { drawFramebuffer(it) }
  }

  private fun setupFramebuffer(): Framebuffer {
    mc.framebuffer.unbindFramebuffer()
    val fb = Framebuffer(textureWidth, textureHeight, true)
    fb.bindFramebuffer(true)
    GlStateManager.clear(256)
    GlStateManager.matrixMode(5889)
    GlStateManager.pushMatrix()
    GlStateManager.loadIdentity()
    GlStateManager.ortho(0.0, fb.framebufferWidth.toDouble(), fb.framebufferHeight.toDouble(), 0.0, 1000.0, 3000.0)
    GlStateManager.matrixMode(5888)
    GlStateManager.pushMatrix()
    GlStateManager.loadIdentity()
    GlStateManager.translate(0.0, 0.0, -2000.0)
    return fb
  }

  private fun drawFramebuffer(fb: Framebuffer) {
    fb.unbindFramebuffer()
    mc.framebuffer.bindFramebuffer(true)
    GlStateManager.matrixMode(5889)
    GlStateManager.popMatrix()
    GlStateManager.matrixMode(5888)
    GlStateManager.popMatrix()

    fb.bindFramebufferTexture()
    GL20.glUseProgram(standardShader)
    val falloffcenter = (Long.MAX_VALUE - System.currentTimeMillis()) / 50 % (textureHeight + 40) - 20
    val falloffcenter2 = ((Long.MAX_VALUE - System.currentTimeMillis()) / 50 + 500) % (textureHeight + 40) - 20
    GL20.glUniform1i(GL20.glGetUniformLocation(standardShader, "falloffcenter[0]"), falloffcenter.toInt())
    GL20.glUniform1i(GL20.glGetUniformLocation(standardShader, "falloffcenter[1]"), falloffcenter2.toInt())
    GL20.glUniform1i(GL20.glGetUniformLocation(standardShader, "tex"), 0)
    GL20.glUniform2f(GL20.glGetUniformLocation(standardShader, "size"), textureWidth.toFloat(), textureHeight.toFloat())

    val tessellator = Tessellator.getInstance()
    val vertexbuffer = tessellator.buffer

    vertexbuffer.begin(7, DefaultVertexFormats.POSITION)
    val fbw = fb.framebufferWidth.toDouble()
    val fbh = fb.framebufferHeight.toDouble()
    vertexbuffer.pos(0.0, fbh, 0.0).endVertex()
    vertexbuffer.pos(fbw, fbh, 0.0).endVertex()
    vertexbuffer.pos(fbw, 0.0, 0.0).endVertex()
    vertexbuffer.pos(0.0, 0.0, 0.0).endVertex()
    tessellator.draw()
    fb.unbindFramebufferTexture()
    fb.unbindFramebuffer()
    fb.deleteFramebuffer()
    mc.framebuffer.bindFramebuffer(true)
    GL20.glUseProgram(0)
  }

  companion object {
    var supportsShader = true

    val tilesetData by lazy {
      val i = TextureUtil.glGenTextures()
      TextureUtil.allocateTexture(i, 8, 2048)
      i
    }

    private val standardShader by lazy {
      val cl = Screen::class.java.classLoader
      linkShaders(
        loadShader(cl.getResourceAsStream("assets/$ModID/shaders/screen.vert"), GL20.GL_VERTEX_SHADER),
        loadShader(cl.getResourceAsStream("assets/$ModID/shaders/screen.frag"), GL20.GL_FRAGMENT_SHADER)
      )
    }

    private fun linkShaders(vert: Int, frag: Int): Int {
      RetroComputers.Logger.info("Linking shaders...")
      val prog = GL20.glCreateProgram()
      if (prog == 0) {
        supportsShader = false
        return 0
      }
      GL20.glAttachShader(prog, vert)
      GL20.glAttachShader(prog, frag)
      GL20.glLinkProgram(prog)
      logp(prog).lines().forEach(RetroComputers.Logger::info)
      if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
        supportsShader = false
        return 0
      }
      GL20.glValidateProgram(prog)
      logp(prog).lines().forEach(RetroComputers.Logger::info)
      if (GL20.glGetProgrami(prog, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
        supportsShader = false
        return 0
      }
      return prog
    }

    private fun loadShader(inputStream: InputStream, stype: Int): Int {
      val scanner = Scanner(inputStream).useDelimiter("\\Z")
      val source = scanner.next()
      scanner.close()
      val shader = GL20.glCreateShader(stype)
      RetroComputers.Logger.info("Compiling ${
      when (stype) {
        GL20.GL_VERTEX_SHADER -> "vertex"
        GL20.GL_FRAGMENT_SHADER -> "fragment"
        else -> "unknown($stype)"
      }
      } shader...")
      if (shader == 0) {
        supportsShader = false
        return 0
      }
      GL20.glShaderSource(shader, source)
      GL20.glCompileShader(shader)
      if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
        RetroComputers.Logger.info(log(shader))
        supportsShader = false
        return 0
      }
      return shader
    }

    private fun log(shader: Int): String = GL20.glGetShaderInfoLog(shader, 65535)

    private fun logp(program: Int): String = GL20.glGetProgramInfoLog(program, 65535)
  }
}