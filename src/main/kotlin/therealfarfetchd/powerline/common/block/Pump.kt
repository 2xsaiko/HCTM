package therealfarfetchd.powerline.common.block

import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.block.FluidPipeContainer
import therealfarfetchd.powerline.common.block.capability.PumpConnectable
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.qblock.IQBlockRedstone
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.util.math.Vec3

@BlockDef(registerModels = false, creativeTab = ModID)
class Pump : BlockPowered(), IQBlockRedstone {
  override fun canConnect(side: EnumFacing): Boolean = true

  var facing: EnumFacing = EnumFacing.NORTH
    private set

  val fluidIn = FluidPipeContainer(neighborSupport { it.base == facing.opposite && it.side == null })

  val fluidOut = FluidPipeContainer(neighborSupport { it.base == facing && it.side == null })

  var rsPowered: Boolean = false

  var animProgress: Float = 0F

  init {
    worldCL.addProperties(fluidIn::pressure, fluidIn::fluid, fluidIn::amount, fluidOut::pressure, fluidOut::fluid, fluidOut::amount, this::rsPowered, this::animProgress)
    clientCL.addProperties(this::animProgress)
    displayCL.addProperties(this::rsPowered)
  }

  @Suppress("LeakingThis")
  private val connectable =
    EnumFacing.VALUES.map { it to PumpConnectable(it, this) }.toMap()

  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).opposite
  }

  override fun rotateBlock(axis: EnumFacing): Boolean {
    facing = facing.rotateAround(axis.axis)
    dataChanged()
    clientDataChanged()
    world.notifyNeighborsOfStateChange(pos, Block, false)
    return true
  }

  override fun onNeighborChanged(side: EnumFacing) {
    super.onNeighborChanged(side)
    val prevPowered = rsPowered
    rsPowered = world.isBlockPowered(pos)
    if (rsPowered != prevPowered) {
      dataChanged()
      clientDataChanged()
    }
  }

  override fun update() {
    super.update()
    if (world.isServer) {
      if ((rsPowered || animProgress != 0F) && hasPower) {
        animProgress += getPullSpeed() * 0.125F * if (rsPowered) 1F else 0.5F
        while (animProgress > 1) {
          pull()
          if (rsPowered) animProgress -= 1 else animProgress = 0F
        }
      }

      fluidIn.update()
      fluidOut.update()

      val fluid = fluidIn.fluid ?: fluidOut.fluid
      if (fluid != null && fluidIn.fluid ?: fluid == fluidOut.fluid ?: fluid) {
        val packetSize = 1000
        val transferAmount = minOf(fluidOut.accept(packetSize, fluid, simulate = true), -fluidIn.accept(-packetSize, fluid, simulate = true))
        if (transferAmount != 0) {
          fluidOut.accept(transferAmount, fluid, applyPressure = false)
          fluidIn.accept(-transferAmount, fluid, applyPressure = false)
        }
      }
    }
  }

  fun getPullSpeed(): Float {
    val a = Math.pow(fluidIn.pressure * 0.001, 3.0)
    val b = -Math.pow(fluidOut.pressure * 0.001, 3.0)
    val effectivity = (a + b) / 2 + 1
    //    val effectivity = ((-Math.pow(Math.abs(fluidIn.pressure) * 0.001, 2.0) + 1) + (-Math.pow(Math.abs(fluidOut.pressure) * 0.001, 2.0) + 1)) / 2F
    return maxOf(0F, effectivity.toFloat())
  }

  private fun pull() {
    fluidIn.pressure -= 50
    fluidOut.pressure += 50
    cond.applyPower(-25.0)
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["F"] = facing.index
    nbt.bool["P"] = rsPowered
    nbt.float["A"] = animProgress
    fluidIn.save(nbt.nbt["Fi"])
    fluidOut.save(nbt.nbt["Fo"])
    cond.save(nbt.nbt["c"])
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getFront(nbt.ubyte["F"])
    rsPowered = nbt.bool["P"]
    animProgress = nbt.float["A"]
    fluidIn.load(nbt.nbt["Fi"])
    fluidOut.load(nbt.nbt["Fo"])
    cond.load(nbt.nbt["c"])
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    if (capability == Capabilities.Connectable && side != null) return connectable[side] as T
    return super.getCapability(capability, side)
  }

  override fun applyProperties(state: IBlockState): IBlockState =
    super.applyProperties(state)
      .withProperty(PropFacing, facing)
      .withProperty(PropActive, (rsPowered || animProgress != 0F) && hasPower)

  override val properties: Set<IProperty<*>> = super.properties + PropFacing + PropActive

  override val material: Material = Material.IRON
  override val soundType: SoundType = SoundType.METAL
  override val useNeighborBrightness: Boolean = true

  override val isOpaque: Boolean = false

  override fun isSideOpaque(facing: EnumFacing): Boolean = rot(this.facing, facing) == EnumFacing.DOWN

  override val blockType: ResourceLocation = ResourceLocation(ModID, "pump")

  override fun getItem(): ItemStack = Item.makeStack()

  companion object {
    val Block by WrapperImplManager.container(Pump::class)
    val Item by WrapperImplManager.item(Pump::class)

    val PropFacing: PropertyEnum<EnumFacing> = PropertyEnum.create("facing", EnumFacing::class.java)
    val PropActive: PropertyBool = PropertyBool.create("active")

    fun rot(facing: EnumFacing, f: EnumFacing?): EnumFacing? {
      if (f == null) return null

      val v1 = f.directionVec
      var vec = Vec3(v1.x.toFloat(), v1.y.toFloat(), v1.z.toFloat())
      vec = if (facing.axis != EnumFacing.Axis.Y)
        vec.rotate(-facing.horizontalAngle.toDouble(), EnumFacing.Axis.Y, Vec3(0f, 0f, 0f))
      else
        vec.rotate(if (facing == EnumFacing.DOWN) 90.0 else -90.0, EnumFacing.Axis.X, Vec3(0f, 0f, 0f))

      return EnumFacing.getFacingFromVector(vec.xf, vec.yf, vec.zf)
    }
  }
}