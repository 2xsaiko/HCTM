package therealfarfetchd.retrocomputers.client.gui

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.TextureUtil
import net.minecraft.client.gui.Screen
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.util.math.Vec3d
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import therealfarfetchd.retrocomputers.client.init.Shaders
import therealfarfetchd.retrocomputers.common.block.TerminalEntity
import kotlin.experimental.xor

private val buf = BufferUtils.createByteBuffer(16384)

private val vbo = GLX.glGenBuffers()
private val vao = GL30.glGenVertexArrays()
private val screenTex = createTexture()
private val charsetTex = createTexture()

class TerminalScreen(val te: TerminalEntity) : Screen(TranslatableTextComponent("retrocomputers.block.terminal")) {

  private var uCharset = 0
  private var uScreen = 0
  private var aXyz = 0
  private var aUv = 0

  override fun tick() {
    val minecraft = minecraft!!
    val dist = minecraft.player.getCameraPosVec(1f).squaredDistanceTo(Vec3d(te.pos).add(0.5, 0.5, 0.5))
    if (dist > 10 * 10) minecraft.openScreen(null)
  }

  override fun render(mouseX: Int, mouseY: Int, delta: Float) {
    renderBackground()

    val sh = Shaders.screen()

    GLX.glUseProgram(sh)
    GL30.glBindVertexArray(vao)
    GLX.glBindBuffer(GLX.GL_ARRAY_BUFFER, vbo)

    GL30.glEnableVertexAttribArray(aXyz)
    GL30.glEnableVertexAttribArray(aUv)

    GlStateManager.activeTexture(GL13.GL_TEXTURE0)
    GlStateManager.enableTexture()
    GlStateManager.bindTexture(screenTex)
    GLX.glUniform1i(uScreen, 0)

    buf.clear()
    buf.put(te.screen)

    if (te.cm == 1 || (te.cm == 2 && (System.currentTimeMillis() / 500) % 2 == 0L)) {
      val ci = te.cx + te.cy * 80
      buf.put(ci, te.screen[ci] xor 0x80.toByte())
    }

    buf.rewind()
    GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R16I, 80, 60, 0, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_BYTE, buf.asIntBuffer())

    GlStateManager.activeTexture(GL13.GL_TEXTURE2)
    GlStateManager.enableTexture()
    GlStateManager.bindTexture(charsetTex)
    GLX.glUniform1i(uCharset, 2)

    buf.clear()
    // buf.put(ByteArray(8 * 256) { if(it%2==0) 0b01010101 else 0b10101010.toByte() })
    buf.put(te.charset)
    buf.rewind()
    GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R16I, 8, 256, 0, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_BYTE, buf.asIntBuffer())

    GL11.glDrawArrays(GL_TRIANGLES, 0, 6)

    GL30.glDisableVertexAttribArray(aXyz)
    GL30.glDisableVertexAttribArray(aUv)

    GLX.glBindBuffer(GLX.GL_ARRAY_BUFFER, 0)
    GL30.glBindVertexArray(0)
    GLX.glUseProgram(0)

    GlStateManager.bindTexture(0)
    GlStateManager.disableTexture()
    GlStateManager.activeTexture(GL13.GL_TEXTURE0)
    GlStateManager.bindTexture(0)
  }

  override fun init() {
    minecraft!!.keyboard.enableRepeatEvents(true)

    initDrawData()
  }

  private fun initDrawData() {
    val swidth = 8 * 40
    val sheight = 8 * 25
    val x1 = width / 2f - swidth / 2f
    val y1 = height / 2f - sheight / 2f

    val sh = Shaders.screen()

    GLX.glUseProgram(sh)
    GL30.glBindVertexArray(vao)
    GLX.glBindBuffer(GLX.GL_ARRAY_BUFFER, vbo)

    uCharset = GLX.glGetUniformLocation(sh, "charset")
    uScreen = GLX.glGetUniformLocation(sh, "screen")

    aXyz = GLX.glGetAttribLocation(sh, "xyz")
    aUv = GLX.glGetAttribLocation(sh, "uv")

    GL30.glVertexAttribPointer(aXyz, 3, GL_FLOAT, false, 20, 0)
    GL30.glVertexAttribPointer(aUv, 2, GL_FLOAT, false, 20, 12)

    buf.clear()

    // @formatter:off
    floatArrayOf(
      x1,          y1,           0f, 0f, 0f,
      x1 + swidth, y1 + sheight, 0f, 1f, 1f,
      x1 + swidth, y1,           0f, 1f, 0f,

      x1,          y1,           0f, 0f, 0f,
      x1,          y1 + sheight, 0f, 0f, 1f,
      x1 + swidth, y1 + sheight, 0f, 1f, 1f
    ).forEach { buf.putFloat(it) }
    // @formatter:on

    buf.rewind()

    GLX.glBufferData(GLX.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW)

    GLX.glBindBuffer(GLX.GL_ARRAY_BUFFER, 0)
    GL30.glBindVertexArray(0)
    GLX.glUseProgram(0)
  }

  override fun removed() {
    minecraft!!.keyboard.enableRepeatEvents(false)
  }

  override fun isPauseScreen() = false

}

private fun createTexture(): Int {
  val tex = TextureUtil.generateTextureId()
  GlStateManager.bindTexture(tex)
  GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
  GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
  GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
  GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
  GlStateManager.bindTexture(0)
  return tex
}