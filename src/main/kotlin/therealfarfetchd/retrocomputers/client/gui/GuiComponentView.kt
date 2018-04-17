package therealfarfetchd.retrocomputers.client.gui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderHandEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import therealfarfetchd.quacklib.common.api.extensions.rotateY
import therealfarfetchd.quacklib.common.api.extensions.toVec3
import therealfarfetchd.quacklib.common.api.util.math.Mat4
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.quacklib.common.api.util.math.times
import therealfarfetchd.quacklib.common.api.util.math.toVec3
import therealfarfetchd.retrocomputers.common.block.Rack
import therealfarfetchd.retrocomputers.common.util.getRackPos
import kotlin.math.PI
import kotlin.math.cos
import kotlin.reflect.KMutableProperty0

private val buf = BufferUtils.createFloatBuffer(16)


class GuiComponentView(val pos: BlockPos, val component: Int, val side: EnumFacing) : GuiScreen() {
  private lateinit var viewEntity: EntityView
  private var isInit = false

  private var isClosing = false

  private var hitResult: Triple<Rack.Tile, Int, Int>? = null

  private var guiMVP = Mat4.Identity
  private var worldMVP = Mat4.Identity

  override fun doesGuiPauseGame() = false

  override fun updateScreen() {
    super.updateScreen()
    if (isClosing && viewEntity.progress == 0f) closeScreen()
  }

  override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    updateMatrix(mvp = ::guiMVP)
    viewEntity.offsetX = (mouseX / width.toFloat() - 0.5f) * 2
    viewEntity.offsetY = (mouseY / height.toFloat() - 0.5f) * 2

    updateHit(mouseX, mouseY)

    val worldToGui = guiMVP.inverse * worldMVP

    hitResult?.also { (te, slotIndex, bbIndex) ->
      val bb = te.container.getComponent(slotIndex).getBoundingBoxes()[bbIndex]
        .offset(2 / 16.0, 1 / 16.0 + 7 / 16.0 * slotIndex, 0 / 16.0)
        .rotateY(te.facing.opposite).offset(te.pos)

      val points = listOf(
        Vec3(bb.minX, bb.minY, bb.minZ),
        Vec3(bb.maxX, bb.minY, bb.minZ),
        Vec3(bb.minX, bb.maxY, bb.minZ),
        Vec3(bb.maxX, bb.maxY, bb.minZ),
        Vec3(bb.minX, bb.minY, bb.maxZ),
        Vec3(bb.maxX, bb.minY, bb.maxZ),
        Vec3(bb.minX, bb.maxY, bb.maxZ),
        Vec3(bb.maxX, bb.maxY, bb.maxZ)
      )
        .map { worldToGui * it }


      val minX = points.minBy { it.x }!!.xf
      val minY = points.minBy { it.y }!!.yf
      val maxX = points.maxBy { it.x }!!.xf
      val maxY = points.maxBy { it.y }!!.yf

      enableAlpha()
      enableBlend()
      GL11.glLineStipple(2, 0b0101010101010101)
      GL11.glEnable(GL11.GL_LINE_STIPPLE)
      GL11.glLineWidth(2.0f)
      GL11.glColor4f(0.5f, 0.5f, 0.5f, 1f)
      GL11.glBegin(GL11.GL_LINE_STRIP)
      GL11.glVertex2f(minX, minY)
      GL11.glVertex2f(maxX, minY)
      GL11.glVertex2f(maxX, maxY)
      GL11.glVertex2f(minX, maxY)
      GL11.glVertex2f(minX, minY)
      GL11.glEnd()
      GL11.glDisable(GL11.GL_LINE_STIPPLE)
    }
  }

  override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
    hitResult?.also { (te, slotIndex, bbIndex) -> te.clickBoxClient(slotIndex, bbIndex, EnumHand.MAIN_HAND) }
  }

  fun updateHit(mouseX: Int, mouseY: Int) {
    hitResult = null

    val guiToWorld = worldMVP.inverse * guiMVP

    val start = guiToWorld * Vec3(mouseX, mouseY, 0)
    val end = (start - guiToWorld * Vec3(mouseX, mouseY, 10)).normalize() + start
    val rtr = viewEntity.world.rayTraceBlocks(start.toVec3d(), end.toVec3d()) ?: return

    val rackPos = getRackPos(viewEntity.world, rtr.blockPos) ?: return
    val te = viewEntity.world.getTileEntity(rackPos) as? Rack.Tile ?: return

    val (slotIndex, bbIndex) = rtr.hitInfo as? Pair<*, *> ?: return
    slotIndex as? Int ?: return
    bbIndex as? Int ?: return

    hitResult = Triple(te, slotIndex, bbIndex)
  }

  override fun initGui() {
    super.initGui()
    if (isInit) return
    val v = side.directionVec.toVec3()
    val targetPos = pos.toVec3() + Vec3(0.5, (3.5 + 7 * component) / 16.0, 0.5) + v * 0.75f
    viewEntity = EntityView(mc.world, mc.player, targetPos, 180 + side.horizontalAngle, (v.y * 90).toFloat())
    mc.world.spawnEntity(viewEntity)
    mc.renderViewEntity = viewEntity
    isInit = true
    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent
  fun onRenderWorld(e: RenderWorldLastEvent) {
    pushMatrix()
    val pe = interp(Vec3(viewEntity.lastTickPosX, viewEntity.lastTickPosY, viewEntity.lastTickPosZ), viewEntity.positionVector.toVec3(), e.partialTicks)
    translate(-pe.x, -pe.y, -pe.z)
    updateMatrix(mvp = ::worldMVP)
    popMatrix()
  }

  @SubscribeEvent
  fun onRenderHand(e: RenderHandEvent) {
    e.isCanceled = true
  }

  @SubscribeEvent
  fun onRenderGameOverlay(e: RenderGameOverlayEvent) {
    e.isCanceled = true
  }

  fun updateMatrix(mvp: KMutableProperty0<Mat4>) {
    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf)
    val m1 = Mat4.fromBuffer(buf)

    GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf)
    val m2 = Mat4.fromBuffer(buf)

    val mat4 = m2 * m1

    if (mvp.get() != mat4) mvp.set(mat4) // we don't want the inverse matrix to get lost if it didn't change
  }

  override fun onGuiClosed() {
    super.onGuiClosed()
    mc.renderViewEntity = mc.player
    mc.world.removeEntity(viewEntity)
    MinecraftForge.EVENT_BUS.unregister(this)
    isInit = false
  }

  override fun keyTyped(typedChar: Char, keyCode: Int) {
    if (keyCode == 1 && !isClosing) {
      isClosing = true
      viewEntity.speed = -viewEntity.speed
    }
  }

  fun closeScreen() {
    this.mc.displayGuiScreen(null)

    if (this.mc.currentScreen == null) {
      this.mc.setIngameFocus()
    }
  }

  fun interp(prev: Vec3, d: Vec3, p: Float) = prev + (d - prev) * p

  private class EntityView(world: World, player: EntityPlayer, val targetPos: Vec3, targetYaw: Float, val targetPitch: Float) : Entity(world) {
    val originPos = player.getPositionEyes(1f).toVec3() - Vec3(0f, eyeHeight, 0f)
    val originYaw = (player.rotationYawHead % 360 + 360) % 360
    val originPitch = player.rotationPitch

    val targetYaw = (targetYaw % 360 + 360) % 360

    var progress = 0f
    var speed = 0.1f

    var offsetX = 0f
    var offsetY = 0f

    init {
      width = 0.1f
      height = 0.1f
      setLocationAndAngles(originPos.x, originPos.y, originPos.z, originYaw, originPitch)
    }

    override fun onUpdate() {
      super.onUpdate()
      progress = maxOf(0f, minOf(1f, progress + speed))

      val lv = lookVec.toVec3()
      val skewY = Vec3(0, 1, 0)
      val skewX = lv crossProduct skewY

      val p = ((1 - cos(progress * PI)) / 2).toFloat()
      val pos = originPos + (targetPos - originPos) * p + skewX * offsetX * progress * 0.25f + skewY * -offsetY * progress * 0.125f
      val yaw = originYaw + (targetYaw - originYaw) * p
      val pitch = originPitch + (targetPitch - originPitch) * p

      posX = pos.x
      posY = pos.y
      posZ = pos.z
      rotationYaw = yaw
      rotationPitch = pitch
    }

    override fun writeEntityToNBT(compound: NBTTagCompound?) {}

    override fun readEntityFromNBT(compound: NBTTagCompound?) {}

    override fun entityInit() {}

    override fun getEyeHeight() = 0.05f
  }
}