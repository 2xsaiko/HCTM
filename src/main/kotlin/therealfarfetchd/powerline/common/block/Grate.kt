package therealfarfetchd.powerline.common.block

import net.minecraft.block.BlockLiquid
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.IFluidBlock
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.block.FluidPipeContainer
import therealfarfetchd.powerline.common.api.block.capability.SimpleFluidConnectable
import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.QBlock
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

@BlockDef(registerModels = false, creativeTab = ModID)
class Grate : QBlock(), ITickable {

  var facing: EnumFacing = EnumFacing.UP

  val fluid = FluidPipeContainer(INeighborSupport { (f, c) ->
    if (f == facing && c == null)
      world.getTileEntity(pos.offset(f))?.getCapability(Capabilities.Connectable, f.opposite)?.getEdge(null) as? FluidPipeContainer
    else null
  })

  val connectable = SimpleFluidConnectable(fluid)

  val worldCL = ChangeListener(fluid::pressure, fluid::fluid, fluid::amount)

  var cooldown = 0

  init {
    fluid.capacity = 4000
  }

  override fun update() {
    if (world.isServer) {
      fluid.update()

      if (cooldown == 0) {
        if (fluid.pressure > 100) {
          push()
        }
        if (fluid.pressure < -100) {
          pull()
        }
      } else cooldown--

      if (worldCL.valuesChanged()) dataChanged()
    }
  }

  fun push() {
    val f = fluid.fluid
    if (f != null && fluid.canAccept(-1000, f)) {
      val pos = findAirBlock()
      if (pos != null) {
        var block = f.block
        if (block is BlockLiquid) block = BlockLiquid.getFlowingBlock(block.defaultState.material)
        world.setBlockState(pos, block.defaultState)
        fluid.accept(-1000, f)
        return
      }
    }
    fluid.pressure -= 100
    cooldown = 100
  }

  fun pull() {
    val source = findLiquidSource()
    if (source != null) {
      val f = FluidRegistry.lookupFluidForBlock(world.getBlockState(source).block)
      if (f != null) {
        if (fluid.fluid == null || f == fluid.fluid) {
          if (fluid.fluid == null) {
            fluid.fluid = f
            fluid.amount = 0
          }
          if (fluid.canAccept(1000, f)) {
            fluid.accept(1000, f)
            world.setBlockToAir(source)
            return
          }
        }
      }
    }
    fluid.pressure += 100
    cooldown = 100
  }

  fun findAirBlock(): BlockPos? {
    var blocks: Map<BlockPos, IBlockState> = mapOf(pos to container.blockType.defaultState)
    while (blocks.size < 16384) {
      val s = blocks.size
      val r = blocks.entries.sortedBy { it.key.y }.firstOrNull {
        it.value.block.isAir(it.value, world, it.key) ||
        (isBlockLiquid(it.key) && !isBlockLiquidSource(it.key))
      }
      if (r != null) return r.key
      blocks += blocks
        .filter { it.key == pos || FluidRegistry.lookupFluidForBlock(it.value.block) == fluid.fluid }
        .flatMap { (if (it.key == pos) EnumFacing.VALUES.toSet() - facing else EnumFacing.VALUES.toSet()).map { e -> it.key.offset(e) } }
        .filter { it !in blocks.keys }
        .map { it to world.getBlockState(it) }
      if (blocks.size == s) return null
    }
    return null
  }

  fun isBlockLiquidSource(pos: BlockPos): Boolean {
    val state = world.getBlockState(pos)
    val block = state.block
    return when (block) {
      is BlockLiquid -> state[BlockLiquid.LEVEL] == 0
      is IFluidBlock -> block.canDrain(world, pos)
      else -> false
    }
  }

  fun isBlockLiquid(pos: BlockPos): Boolean {
    val block = world.getBlockState(pos).block
    return block is BlockLiquid || block is IFluidBlock
  }

  fun findLiquidSource(): BlockPos? {
    var blocks: Map<BlockPos, IBlockState> = mapOf(pos to container.blockType.defaultState)
    var fluid = fluid.fluid
    while (blocks.size < 16384) {
      val s = blocks.size
      val r = blocks.entries.sortedBy { it.key.y }.firstOrNull { isBlockLiquidSource(it.key) }
      if (r != null) return r.key
      blocks += blocks
        .filter { it.key == pos || (FluidRegistry.lookupFluidForBlock(it.value.block)?.also { if (fluid == null) fluid = it } == fluid && fluid != null) }
        .flatMap { (if (it.key == pos) EnumFacing.VALUES.toSet() - facing else EnumFacing.VALUES.toSet()).map { e -> it.key.offset(e) } }
        .filter { it !in blocks.keys }
        .map { it to world.getBlockState(it) }
      if (blocks.size == s) return null
    }
    return null
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    facing = EnumFacing.getDirectionFromEntityLiving(pos, placer)
  }

  override fun rotateBlock(axis: EnumFacing): Boolean {
    facing = facing.rotateAround(axis.axis)
    dataChanged()
    clientDataChanged()
    world.notifyNeighborsOfStateChange(pos, Pump.Block, false)
    return true
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["F"] = facing.index
    fluid.save(nbt.nbt["Fl"])
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getFront(nbt.ubyte["F"])
    fluid.load(nbt.nbt["Fl"])
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    if (capability == Capabilities.Connectable && side == facing) return connectable as T
    return super.getCapability(capability, side)
  }

  override fun applyProperties(state: IBlockState): IBlockState =
    super.applyProperties(state).withProperty(PropFacing, facing)

  override fun getItem(): ItemStack = Item.makeStack()

  override fun isSideOpaque(facing: EnumFacing): Boolean = facing == this.facing

  override val material: Material = Material.IRON
  override val soundType: SoundType = SoundType.METAL
  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT
  override val properties: Set<IProperty<*>> = super.properties + PropFacing
  override val isOpaque: Boolean = false
  override val blockType: ResourceLocation = ResourceLocation(ModID, "grate")

  companion object {
    val Block by WrapperImplManager.container(Grate::class)
    val Item by WrapperImplManager.item(Grate::class)

    val PropFacing = PropertyEnum.create("facing", EnumFacing::class.java)!!

  }
}