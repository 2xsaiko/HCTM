package therealfarfetchd.retrocomputers.client

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.obj.OBJLoader
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.opengl.GL11
import therealfarfetchd.quacklib.client.api.gui.GuiElementRegistry
import therealfarfetchd.quacklib.client.api.gui.GuiLogicRegistry
import therealfarfetchd.quacklib.client.api.model.registerIconRegister
import therealfarfetchd.quacklib.client.api.qbr.bindSpecialRenderer
import therealfarfetchd.quacklib.client.registerModelBakery
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.quacklib.common.api.util.math.Mat4
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.quacklib.common.api.util.math.times
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.client.gui.BusAddressLogic
import therealfarfetchd.retrocomputers.client.gui.ComputerLogic
import therealfarfetchd.retrocomputers.client.gui.GuiComponentView
import therealfarfetchd.retrocomputers.client.gui.TerminalLogic
import therealfarfetchd.retrocomputers.client.gui.elements.*
import therealfarfetchd.retrocomputers.client.keybind.Keybindings
import therealfarfetchd.retrocomputers.client.model.ModelRedstonePort
import therealfarfetchd.retrocomputers.client.model.ModelRedstonePortAnalog
import therealfarfetchd.retrocomputers.client.render.block.RackRenderer
import therealfarfetchd.retrocomputers.client.render.block.RetinalScannerRenderer
import therealfarfetchd.retrocomputers.common.Proxy
import therealfarfetchd.retrocomputers.common.block.*
import therealfarfetchd.retrocomputers.common.item.ItemDebug
import therealfarfetchd.retrocomputers.common.util.getRackPos

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
    ClientRegistry.bindTileEntitySpecialRenderer(Rack.Tile::class.java, RackRenderer)

    OBJLoader.INSTANCE.addDomain(ModID)
  }

  @SubscribeEvent
  fun registerModels(event: ModelRegistryEvent) {
    for (it in 0..RetroComputers.disks.streams.size) {
      ModelLoader.setCustomModelResourceLocation(RetroComputers.disks, it, ModelResourceLocation("retrocomputers:system_disk", "inventory"))
    }

    ModelLoader.setCustomModelResourceLocation(Rack.Item, 0, ModelResourceLocation("retrocomputers:rack", "inventory"))

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

  @SubscribeEvent
  fun drawBlockOutline(e: DrawBlockHighlightEvent) {
    val player = e.player
    val world = player.world
    val pos = e.target.blockPos
    val rackPos = getRackPos(world, pos) ?: return
    val te = world.getTileEntity(rackPos) as Rack.Tile

    fun AxisAlignedBB.rotateY(facing: EnumFacing): AxisAlignedBB {
      val transform = Mat4.Identity
        .translate(0.5f, 0.5f, 0.5f)
        .rotate(0f, 1f, 0f, facing.horizontalAngle)
        .translate(-0.5f, -0.5f, -0.5f)

      val v1 = transform * Vec3(minX, minY, minZ)
      val v2 = transform * Vec3(maxX, maxY, maxZ)

      return AxisAlignedBB(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z)
    }

    val (slotIndex, bbIndex) = e.target.hitInfo as? Pair<*, *> ?: return
    slotIndex as? Int ?: return
    bbIndex as? Int ?: return

    val bb = te.container.getComponent(slotIndex).getBoundingBoxes()[bbIndex]

    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
    GlStateManager.glLineWidth(2.0f)
    GlStateManager.disableTexture2D()
    GlStateManager.depthMask(false)

    if (world.worldBorder.contains(rackPos)) {
      val x = player.lastTickPosX + (player.posX - player.lastTickPosX) * e.partialTicks
      val y = player.lastTickPosY + (player.posY - player.lastTickPosY) * e.partialTicks
      val z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * e.partialTicks
      drawSelectionBoundingBox(bb.offset(2 / 16.0, 1 / 16.0 + 7 / 16.0 * slotIndex, 0 / 16.0)
        .rotateY(te.facing).grow(0.002).offset(-x, -y, -z).offset(rackPos),
        0.0f, 0.0f, 0.0f, 0.4f)
    }

    GlStateManager.depthMask(true)
    GlStateManager.enableTexture2D()
    GlStateManager.disableBlend()

    e.isCanceled = true
  }

  @SubscribeEvent
  fun onKeyInput(e: InputEvent.KeyInputEvent) {
    if (Keybindings.ComponentView.isPressed && mc.currentScreen == null) {
      val target = mc.objectMouseOver
      val (slotIndex, _) = target.hitInfo as? Pair<*, *> ?: return
      slotIndex as? Int ?: return

      val pos = target.blockPos
      val rackPos = getRackPos(mc.world, pos) ?: return

      mc.world.getTileEntity(rackPos) as? Rack.Tile ?: return

      mc.displayGuiScreen(GuiComponentView(rackPos, slotIndex, target.sideHit))
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
    //          }.forEach { data ->
    //            if (data is RibbonCable.CableConn) {
    //              fun plane(x: Double, y: Double, width: Double, height: Double) {
    //                val vec = f.directionVec
    //                val yOffset = (data.base.axisDirection.offset + 1) / 2
    //                var realx = 0.0
    //                var realy = 0.0
    //                var realz = 0.0
    //                var realw = 0.0
    //                var realh = 0.0
    //                var reald = 0.0
    //
    //                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    //                when (data.base.axis) {
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
    //              val bbs = data.elements.map { it.first.flip(f, data.base) }
    //              bbs.forEach { plane(it.x.toDouble(), it.y.toDouble(), it.width.toDouble(), it.height.toDouble()) }
    //            }
    //          }
    //        }
    //      }
    //    }
  }
}