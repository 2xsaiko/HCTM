package therealfarfetchd.hctm.client.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import therealfarfetchd.hctm.client.wire.ClientNetworkState
import therealfarfetchd.hctm.common.wire.NetLink
import therealfarfetchd.hctm.common.wire.NetNode
import therealfarfetchd.hctm.common.wire.WireNetworkController
import java.awt.Color
import java.util.*
import kotlin.streams.asSequence

fun draw(delta: Float) {
  return
  val mc = MinecraftClient.getInstance()
  val cam = mc.gameRenderer.camera
  // val camEnt = mc.cameraEntity ?: mc.player
  // val camEntPos = camEnt.getCameraPosVec(delta).subtract(0.0, camEnt.getEyeHeight(camEnt.pose).toDouble(), 0.0)
  val controller = ClientNetworkState.request(mc.world) ?: return
  val ndp = NodeDrawPositioner(controller)

  GlStateManager.pushMatrix()

  GlStateManager.disableTexture()
  GlStateManager.disableDepthTest()
  GlStateManager.depthMask(false)
  //  GlStateManager.disableCull()
  GlStateManager.enableBlend()
  GlStateManager.blendFunc(ONE, ONE_MINUS_SRC_COLOR)

  GlStateManager.translated(-cam.pos.x, -cam.pos.y, -cam.pos.z)

  val t = Tessellator.getInstance()
  val buf = t.bufferBuilder

  for (network in controller.getNetworks()) {
    val color = getNetworkColor(network.id)
    GlStateManager.color4f(color.red / 255f, color.green / 255f, color.blue / 255f, 0.5f)
    buf.begin(GL11.GL_LINES, VertexFormats.POSITION)
    for (node in network.getNodes()) {
      for (link in node.connections) {
        if (link.first == node) {
          drawLink(buf, link, ndp)
        }
      }
    }
    t.draw()
  }

  for (network in controller.getNetworks()) {
    val color = getNetworkColor(network.id)
    GlStateManager.color4f(color.red / 255f, color.green / 255f, color.blue / 255f, 0.5f)
    buf.begin(GL11.GL_QUADS, VertexFormats.POSITION)
    for (node in network.getNodes()) {
      drawNode(buf, node, ndp)
    }
    t.draw()
  }
  GlStateManager.popMatrix()
}

private fun getNetworkColor(id: UUID): Color {
  val rnd = Random(id.leastSignificantBits xor id.mostSignificantBits)
  val (r, g, b) = rnd.doubles().asSequence().take(3).toList()
  return Color(r.toFloat(), g.toFloat(), b.toFloat())
}

private fun drawNode(buf: BufferBuilder, node: NetNode, ndp: NodeDrawPositioner) {
  val pos = node.data.pos
  val size = 1 / 16.0
  val offset = ndp.getNodeOffset(node)
  val cX = pos.x + 0.5 + offset.x
  val cY = pos.y + 0.5 + offset.y
  val cZ = pos.z + 0.5 + offset.z

  buf.vertex(cX - size, cY - size, cZ - size).next()
  buf.vertex(cX - size, cY + size, cZ - size).next()
  buf.vertex(cX + size, cY + size, cZ - size).next()
  buf.vertex(cX + size, cY - size, cZ - size).next()

  buf.vertex(cX - size, cY - size, cZ - size).next()
  buf.vertex(cX + size, cY - size, cZ - size).next()
  buf.vertex(cX + size, cY - size, cZ + size).next()
  buf.vertex(cX - size, cY - size, cZ + size).next()

  buf.vertex(cX - size, cY - size, cZ - size).next()
  buf.vertex(cX - size, cY - size, cZ + size).next()
  buf.vertex(cX - size, cY + size, cZ + size).next()
  buf.vertex(cX - size, cY + size, cZ - size).next()

  buf.vertex(cX - size, cY - size, cZ + size).next()
  buf.vertex(cX + size, cY - size, cZ + size).next()
  buf.vertex(cX + size, cY + size, cZ + size).next()
  buf.vertex(cX - size, cY + size, cZ + size).next()

  buf.vertex(cX - size, cY + size, cZ - size).next()
  buf.vertex(cX - size, cY + size, cZ + size).next()
  buf.vertex(cX + size, cY + size, cZ + size).next()
  buf.vertex(cX + size, cY + size, cZ - size).next()

  buf.vertex(cX + size, cY - size, cZ - size).next()
  buf.vertex(cX + size, cY + size, cZ - size).next()
  buf.vertex(cX + size, cY + size, cZ + size).next()
  buf.vertex(cX + size, cY - size, cZ + size).next()
}

private fun drawLink(buf: BufferBuilder, link: NetLink, ndp: NodeDrawPositioner) {
  val pos1 = link.first.data.pos
  val pos2 = link.second.data.pos
  val offset1 = ndp.getNodeOffset(link.first)
  val offset2 = ndp.getNodeOffset(link.second)
  buf.vertex(pos1.x + 0.5 + offset1.x, pos1.y + 0.5 + offset1.y, pos1.z + 0.5 + offset1.z).next()
  buf.vertex(pos2.x + 0.5 + offset2.x, pos2.y + 0.5 + offset2.y, pos2.z + 0.5 + offset2.z).next()
}

class NodeDrawPositioner(controller: WireNetworkController) {
  val nodeCountsAtPos = controller.getNetworks()
    .flatMap { it.getNodes() }
    .map { it.data.pos }
    .let { posList -> posList.distinct().associate { pos -> pos to posList.count { it == pos } } }

  val currentCounts = mutableMapOf<BlockPos, Int>()

  val nodePos = mutableMapOf<NetNode, Int>()

  fun getNodeOffset(node: NetNode): Vec3d {
    val total = nodeCountsAtPos[node.data.pos] ?: 0
    val dist = 0.125

    val index = nodePos.computeIfAbsent(node) { t ->
      val index = currentCounts.compute(node.data.pos) { _, u -> (u ?: -1) + 1 } ?: 0
      index
    }

    val n = index * dist - (total - 1) * dist / 2

    return Vec3d(n, n, n)
  }
}