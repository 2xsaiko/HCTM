package therealfarfetchd.retrocomputers.client.render.block

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.qbr.QBlockSpecialRenderer
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.RetinalScanner

object RetinalScannerRenderer : QBlockSpecialRenderer<RetinalScanner>(), IIconRegister {
  private lateinit var textureRed: TextureAtlasSprite
  private lateinit var textureBlue: TextureAtlasSprite

  private val vertexMap: Map<EnumFacing, List<Double>> = mapOf(
    NORTH to listOf(
      1.0, 1.0, -0.001, 0.0, 0.0,
      1.0, 0.0, -0.001, 0.0, 16.0,
      0.0, 0.0, -0.001, 16.0, 16.0,
      0.0, 1.0, -0.001, 16.0, 0.0
    ),
    SOUTH to listOf(
      0.0, 0.0, 1.001, 0.0, 16.0,
      1.0, 0.0, 1.001, 16.0, 16.0,
      1.0, 1.0, 1.001, 16.0, 0.0,
      0.0, 1.0, 1.001, 0.0, 0.0
    ),
    WEST to listOf(
      -0.001, 1.0, 1.0, 16.0, 0.0,
      -0.001, 1.0, 0.0, 0.0, 0.0,
      -0.001, 0.0, 0.0, 0.0, 16.0,
      -0.001, 0.0, 1.0, 16.0, 16.0
    ),
    EAST to listOf(
      1.001, 0.0, 0.0, 16.0, 16.0,
      1.001, 1.0, 0.0, 16.0, 0.0,
      1.001, 1.0, 1.0, 0.0, 0.0,
      1.001, 0.0, 1.0, 0.0, 16.0
    )
  )

  override fun render(block: RetinalScanner, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) {
    if (block.renderState == 0) return
    setLightmapDisabled(true)

    GlStateManager.pushMatrix()
    GlStateManager.translate(x, y, z)

    GlStateManager.disableLighting()
    val t = Tessellator.getInstance()
    val buffer = t.buffer
    buffer.begin(7, DefaultVertexFormats.POSITION_TEX)

    bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GlStateManager.enableBlend()

    if (Minecraft.isAmbientOcclusionEnabled()) {
      GlStateManager.shadeModel(GL11.GL_SMOOTH)
    } else {
      GlStateManager.shadeModel(GL11.GL_FLAT)
    }

    val tex = when (block.renderState) {
      1 -> textureBlue
      2 -> textureRed
      else -> error("What!?")
    }

    val v = vertexMap[block.facing]!!
    for (i in (0..3).map { it * 5 }) {
      buffer
        .pos(v[i], v[i + 1], v[i + 2])
        .tex(tex.getInterpolatedU(v[i + 3]).toDouble(), tex.getInterpolatedV(v[i + 4]).toDouble())
        .endVertex()
    }
    t.draw()

    GlStateManager.popMatrix()

    GlStateManager.enableLighting()
    setLightmapDisabled(false)
  }

  override fun registerIcons(textureMap: TextureMap) {
    textureRed = textureMap.registerSprite(ResourceLocation(ModID, "blocks/retinal_scanner_red"))
    textureBlue = textureMap.registerSprite(ResourceLocation(ModID, "blocks/retinal_scanner_blue"))
  }
}