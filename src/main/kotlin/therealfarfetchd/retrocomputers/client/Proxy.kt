package therealfarfetchd.retrocomputers.client

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import therealfarfetchd.quacklib.client.api.gui.GuiElementRegistry
import therealfarfetchd.quacklib.client.api.gui.GuiLogicRegistry
import therealfarfetchd.quacklib.client.api.model.registerIconRegister
import therealfarfetchd.quacklib.client.api.qbr.bindSpecialRenderer
import therealfarfetchd.quacklib.client.registerModelBakery
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.client.gui.BusAddressLogic
import therealfarfetchd.retrocomputers.client.gui.ComputerLogic
import therealfarfetchd.retrocomputers.client.gui.TerminalLogic
import therealfarfetchd.retrocomputers.client.gui.elements.*
import therealfarfetchd.retrocomputers.client.model.ModelRedstonePort
import therealfarfetchd.retrocomputers.client.model.ModelRedstonePortAnalog
import therealfarfetchd.retrocomputers.client.render.block.RetinalScannerRenderer
import therealfarfetchd.retrocomputers.common.Proxy
import therealfarfetchd.retrocomputers.common.block.RedstonePort
import therealfarfetchd.retrocomputers.common.block.RedstonePortAnalog
import therealfarfetchd.retrocomputers.common.block.RetinalScanner
import therealfarfetchd.retrocomputers.common.block.RibbonCable
import therealfarfetchd.retrocomputers.common.item.ItemDebug

/**
 * Created by marco on 25.06.17.
 */
class Proxy : Proxy() {

  val mc: Minecraft by lazy { Minecraft.getMinecraft() }

  override fun preInit(e: FMLPreInitializationEvent) {
    super.preInit(e)

    with(GuiElementRegistry) {
      register("$ModID:address_board", AddressBoard::class)
      register("$ModID:red_button", RedButton::class)
      register("$ModID:flip_switch", FlipSwitch::class)
      register("$ModID:light", Light::class)
      register("$ModID:screen", Screen::class)
    }

    with(GuiLogicRegistry) {
      register("$ModID:bus_address", BusAddressLogic::class)
      register("$ModID:computer", ComputerLogic::class)
      register("$ModID:terminal", TerminalLogic::class)
    }

    RetinalScanner::class.bindSpecialRenderer(RetinalScannerRenderer)
  }

  @SubscribeEvent
  fun registerModels(event: ModelRegistryEvent) {
    for (it in 0..RetroComputers.disks.streams.size) {
      ModelLoader.setCustomModelResourceLocation(RetroComputers.disks, it, ModelResourceLocation("retrocomputers:system_disk", "inventory"))
    }

    RetinalScannerRenderer.registerIconRegister()

    registerModelBakery(RedstonePortAnalog.Block, RedstonePortAnalog.Item, ModelRedstonePortAnalog)
    registerModelBakery(RibbonCable.Block, RibbonCable.Item, RibbonCable.Bakery)
    if (FeatureManager.dependSoft(DefaultFeatures.VirtualBundledCable))
      registerModelBakery(RedstonePort.Block, RedstonePort.Item, ModelRedstonePort)
  }

  @SubscribeEvent
  fun onRenderWorld(event: RenderWorldLastEvent) {
    if (mc.player.inventory.getCurrentItem().item == ItemDebug) {
      val mo = mc.objectMouseOver
      if (mo.typeOfHit == RayTraceResult.Type.BLOCK) {
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GL11.glLineWidth(1.0f)
        GlStateManager.glBegin(GL11.GL_LINES)
        drawConnectionBB(mo.blockPos, event.partialTicks)
        GL11.glEnd()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
      }
    }
  }

  private fun drawConnectionBB(pos: BlockPos, partialTicks: Float) {
    //    val e = mc.renderViewEntity ?: return
    //    val pe = e.getPositionEyes(partialTicks)
    //    val offsetX = (-pe.x + pos.x).toFloat()
    //    val offsetY = (-pe.y + pos.y + e.eyeHeight).toFloat()
    //    val offsetZ = (-pe.z + pos.z).toFloat()
    //
    //    fun vertex(x: Float, y: Float, z: Float) = GlStateManager.glVertex3f(offsetX + x, offsetY + y, offsetZ + z)
    //
    //    fun rect(x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, secondPass: Boolean = false, mark: Boolean = true) {
    //      if (width != 0.0f) {
    //        vertex(x, y, z)
    //        vertex(x + width, y, z)
    //      }
    //      if (height != 0.0f) {
    //        vertex(x, y, z)
    //        vertex(x, y + height, z)
    //      }
    //      if (depth != 0.0f) {
    //        vertex(x, y, z)
    //        vertex(x, y, z + depth)
    //      }
    //      if (mark) {
    //        if (width == 0.0f) {
    //          val a = minOf(height, depth) * 0.125f
    //          val ctry = y + height / 2.0f
    //          val ctrz = z + depth / 2.0f
    //          vertex(x, ctry - a, ctrz - a)
    //          vertex(x, ctry + a, ctrz + a)
    //          vertex(x, ctry + a, ctrz - a)
    //          vertex(x, ctry - a, ctrz + a)
    //        }
    //        if (height == 0.0f) {
    //          val a = minOf(width, depth) * 0.125f
    //          val ctrx = x + width / 2.0f
    //          val ctrz = z + depth / 2.0f
    //          vertex(ctrx - a, y, ctrz - a)
    //          vertex(ctrx + a, y, ctrz + a)
    //          vertex(ctrx + a, y, ctrz - a)
    //          vertex(ctrx - a, y, ctrz + a)
    //        }
    //        if (depth == 0.0f) {
    //          val a = minOf(width, height) * 0.125f
    //          val ctrx = x + width / 2.0f
    //          val ctry = y + height / 2.0f
    //          vertex(ctrx - a, ctry - a, z)
    //          vertex(ctrx + a, ctry + a, z)
    //          vertex(ctrx + a, ctry - a, z)
    //          vertex(ctrx - a, ctry + a, z)
    //        }
    //      }
    //      if (!secondPass) rect(x + width, y + height, z + depth, -width, -height, -depth, true, false)
    //    }
    //
    //    val tile = mc.world.getTileEntity(pos)
    //    if (tile != null) {
    //      GlStateManager.color(0.5f, 1.0f, 0.0f, 1.0f)
    //      for (f in EnumFacing.values()) {
    //        if (tile.hasCapability(Capabilities.CompNet, f)) {
    //          val cap = tile.getCapability(Capabilities.CompNet, f)!!
    //          cap.elements
    //            .map { it.first }
    //            .map { FlatBoundingBox(it.x + 0.025f, it.y + 0.025f, it.x + it.width - 0.025f, it.y + it.height - 0.025f) }
    //            .forEach {
    //              when (f) {
    //                EnumFacing.UP -> rect(it.x, 1.0f, it.y, it.width, 0.0f, it.height)
    //                EnumFacing.DOWN -> rect(it.x, 0.0f, it.y, it.width, 0.0f, it.height)
    //                EnumFacing.NORTH -> rect(it.x, it.y, 0.0f, it.width, it.height, 0.0f)
    //                EnumFacing.SOUTH -> rect(it.x, it.y, 1.0f, it.width, it.height, 0.0f)
    //                EnumFacing.WEST -> rect(0.0f, it.y, it.x, 0.0f, it.height, it.width)
    //                EnumFacing.EAST -> rect(1.0f, it.y, it.x, 0.0f, it.height, it.width)
    //              }
    //            }
    //        }
    //      }
    //      GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f)
    //      for (f in EnumFacing.values()) {
    //        if (tile.hasCapability(Capabilities.CompNet, f)) {
    //          val cap = tile.getCapability(Capabilities.CompNet, f)!!
    //          when (cap) {
    //            is JoinedCablePort -> cap.unwrapped
    //            else -> setOf(cap)
    //          }.forEach { cc ->
    //            if (cc is RibbonCable.CableConn) {
    //              fun plane(x: Double, y: Double, width: Double, height: Double) {
    //                val vec = f.directionVec
    //                val yOffset = (cc.base.axisDirection.offset + 1) / 2
    //                var realx = 0.0
    //                var realy = 0.0
    //                var realz = 0.0
    //                var realw = 0.0
    //                var realh = 0.0
    //                var reald = 0.0
    //
    //                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    //                when (cc.base.axis) {
    //                  X -> {
    //                    realz = x
    //                    realy = y
    //                    reald = width
    //                    realh = height
    //                    realx = yOffset.toDouble()
    //                  }
    //                  Y -> {
    //                    realx = x
    //                    realz = y
    //                    realw = width
    //                    reald = height
    //                    realy = yOffset.toDouble()
    //                  }
    //                  Z -> {
    //                    realx = x
    //                    realy = y
    //                    realw = width
    //                    realh = height
    //                    realz = yOffset.toDouble()
    //                  }
    //                }
    //                rect((realx + vec.x).toFloat(), (realy + vec.y).toFloat(), (realz + vec.z).toFloat(), realw.toFloat(), realh.toFloat(), reald.toFloat())
    //              }
    //
    //              val bbs = cc.elements.map { it.first.flip(f, cc.base) }
    //              bbs.forEach { plane(it.x.toDouble(), it.y.toDouble(), it.width.toDouble(), it.height.toDouble()) }
    //            }
    //          }
    //        }
    //      }
    //    }
  }

}