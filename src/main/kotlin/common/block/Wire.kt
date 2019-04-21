package therealfarfetchd.retrocomputers.common.block

import net.minecraft.advancement.criterion.Criterions
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.WallMountedBlock
import net.minecraft.entity.VerticalEntityPosition
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.block.BlockItem
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateFactory.Builder
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.util.math.Direction.EAST
import net.minecraft.util.math.Direction.NORTH
import net.minecraft.util.math.Direction.SOUTH
import net.minecraft.util.math.Direction.UP
import net.minecraft.util.math.Direction.WEST
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.ViewableWorld
import net.minecraft.world.World
import therealfarfetchd.retrocomputers.common.block.ext.BlockCustomBreak
import therealfarfetchd.retrocomputers.common.block.wire.BlockPartProvider
import therealfarfetchd.retrocomputers.common.block.wire.NetNode
import therealfarfetchd.retrocomputers.common.block.wire.NodeView
import therealfarfetchd.retrocomputers.common.block.wire.PartExt
import therealfarfetchd.retrocomputers.common.block.wire.getWireNetworkState
import therealfarfetchd.retrocomputers.common.init.Blocks
import therealfarfetchd.retrocomputers.common.init.Items
import net.minecraft.block.Blocks as MCBlocks

class WireBlock : Block(Block.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f)), BlockCustomBreak, BlockPartProvider {

  val boxHeight = 1 / 16f

  init {
    defaultState =
      WireProperties.PlacedWires.values.fold(defaultState) { state, prop -> state.with(prop, false) }
  }

  override fun appendProperties(b: Builder<Block, BlockState>) {
    for (prop in WireProperties.PlacedWires.values) {
      b.with(prop)
    }
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
    return super.getPlacementState(ctx)?.with(WireProperties.PlacedWires.getValue(ctx.facing.opposite), true)
  }

  override fun canPlaceAt(state: BlockState, world: ViewableWorld, pos: BlockPos): Boolean {
    return WireUtils.getOccupiedSides(state).all { side -> WallMountedBlock.method_20046(world, pos, side) }
  }

  private fun getShape(state: BlockState): VoxelShape {
    return WireProperties.PlacedWires.entries
      .filter { (_, prop) -> state[prop] }
      .map { (a, _) -> WireUtils.getShapeForSide(boxHeight.toDouble(), a) }
      .fold(VoxelShapes.empty(), VoxelShapes::union)
  }

  override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, vep: VerticalEntityPosition?): VoxelShape {
    return getShape(state)
  }

  override fun getRayTraceShape(state: BlockState, view: BlockView, pos: BlockPos): VoxelShape {
    return getShape(state)
  }

  override fun tryBreak(state: BlockState, pos: BlockPos, world: World, player: PlayerEntity): Boolean {
    if (WireUtils.getOccupiedSides(state).size < 2) return true

    val dist = if (player.isCreative) 5.0f else 4.5f
    val from = player.getCameraPosVec(0f)
    val dir = player.getRotationVec(0f)
    val to = from.add(dir.x * dist, dir.y * dist, dir.z * dist)
    val (side, _) = WireUtils.rayTrace(state, pos, from, to) ?: return true

    breakPart(state, pos, world, player, side)
    return false
  }

  private fun breakPart(state: BlockState, pos: BlockPos, world: World, player: PlayerEntity, side: Direction): Boolean {
    if (!state[WireProperties.PlacedWires.getValue(side)]) return false

    val result = world.setBlockState(pos, state.with(WireProperties.PlacedWires.getValue(side), false))

    if (result) {
      onBreak(world, pos, getStateForSide(side), player) // particle
      dropStack(world, pos, Items.Wire.defaultStack)
    }

    return result
  }

  override fun method_9517(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
    if (!world.isClient && world is ServerWorld)
      world.getWireNetworkState().onBlockChanged(pos, state)
  }

  override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt<out Any?>> {
    return WireUtils.getOccupiedSides(state).map(::WirePartExt).toSet()
  }

  private fun getStateForSide(vararg side: Direction): BlockState =
    if (side.isEmpty()) MCBlocks.AIR.defaultState else
      side.fold(defaultState) { state, s -> state.with(WireProperties.PlacedWires.getValue(s), true) }

  override fun getStateForNeighborUpdate(state: BlockState, side: Direction, state1: BlockState, world: IWorld, pos: BlockPos, pos1: BlockPos): BlockState {
    return getStateForSide(*WireUtils.getOccupiedSides(state).filter { it != side || getStateForSide(it).canPlaceAt(world, pos) }.toTypedArray())
  }

}

data class WirePartExt(val side: Direction) : PartExt<Direction> {
  override val data: Direction
    get() = side

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    // For now, planar connections only because of simplicity
    return Direction.values().filter { it.axis != side.axis }
      .mapNotNull { d -> nv.getNodes(pos.offset(d)).firstOrNull { it.data.ext is WirePartExt && it.data.ext.side == side } }.toSet()
  }
}

class WireItem : BlockItem(Blocks.Wire, Settings()) {

  override fun place(ctx: ItemPlacementContext): ActionResult {
    val state = this.getBlockState(ctx) ?: return ActionResult.PASS

    if (!doPlace(ctx, state)) return ActionResult.PASS

    return ActionResult.SUCCESS
  }

  private fun tryFit(state: BlockState, new: BlockState): BlockState? {
    if (state.block !is WireBlock || state.block != new.block) return null
    val v1 = WireUtils.getOccupiedSides(state)
    val v2 = WireUtils.getOccupiedSides(new)
    if ((v1 + v2).size != v1.size + v2.size) return null
    return v2.fold(state) { s, side -> s.with(WireProperties.PlacedWires.getValue(side), true) }
  }

  private fun placePart(ctx: ItemPlacementContext, state: BlockState): Boolean {
    val old = ctx.world.getBlockState(ctx.blockPos)
    val combined = tryFit(old, state) ?: return false
    return this.setBlockState(ctx, combined)
  }

  private fun placeBlock(ctx: ItemPlacementContext, state: BlockState): Boolean {
    if (!ctx.canPlace()) return false
    return this.setBlockState(ctx, state)
  }

  private fun doPlace(ctx: ItemPlacementContext, state: BlockState): Boolean {
    if (
      !placePart(ctx, state) &&
      !placeBlock(ctx, state)
    ) return false

    val pos = ctx.blockPos
    val world = ctx.world
    val player = ctx.player
    val stack = ctx.itemStack
    val placedState = world.getBlockState(pos)
    val block = placedState.block
    if (block === state.block) {
      block.onPlaced(world, pos, placedState, player, stack)
      if (player is ServerPlayerEntity) {
        Criterions.PLACED_BLOCK.handle((player as ServerPlayerEntity?)!!, pos, stack)
      }
    }

    val sg = placedState.soundGroup
    world.playSound(player, pos, this.method_19260(placedState), SoundCategory.BLOCKS, (sg.getVolume() + 1.0f) / 2.0f, sg.getPitch() * 0.8f)
    stack.subtractAmount(1)
    return true
  }

}

object WireProperties {
  val PlacedWires = mapOf(
    UP to Properties.UP_BOOL,
    DOWN to Properties.DOWN_BOOL,
    NORTH to Properties.NORTH_BOOL,
    SOUTH to Properties.SOUTH_BOOL,
    EAST to Properties.EAST_BOOL,
    WEST to Properties.WEST_BOOL
  )
}

object WireUtils {
  @Suppress("UNCHECKED_CAST")
  fun rayTrace(state: BlockState, pos: BlockPos, from: Vec3d, to: Vec3d): Pair<Direction, BlockHitResult>? {
    val block = state.block as WireBlock

    return WireProperties.PlacedWires.entries.asSequence()
      .filter { (_, prop) -> state[prop] }
      .map { (a, _) -> Pair(a, getShapeForSide(block.boxHeight.toDouble(), a)) }
      .map { (a, s) -> Pair(a, s.rayTrace(from, to, pos)) }
      .filter { it.second != null }
      .minBy { (_, bhr) -> bhr!!.pos.distanceTo(from) }
      as Pair<Direction, BlockHitResult>?
  }

  fun getShapeForSide(boxHeight: Double, facing: Direction): VoxelShape {
    return when (facing) {
      DOWN -> VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, boxHeight, 1.0)
      UP -> VoxelShapes.cuboid(0.0, 1 - boxHeight, 0.0, 1.0, 1.0, 1.0)
      NORTH -> VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, boxHeight)
      SOUTH -> VoxelShapes.cuboid(0.0, 0.0, 1 - boxHeight, 1.0, 1.0, 1.0)
      WEST -> VoxelShapes.cuboid(0.0, 0.0, 0.0, boxHeight, 1.0, 1.0)
      EAST -> VoxelShapes.cuboid(1 - boxHeight, 0.0, 0.0, 1.0, 1.0, 1.0)
    }
  }

  fun getOccupiedSides(state: BlockState): Set<Direction> {
    return WireProperties.PlacedWires.entries
      .filter { (_, prop) -> state[prop] }
      .map { it.key }
      .toSet()
  }
}

data class WireConnection(val side: Direction, val to: Direction) {

  val isValid = side.axis != to.axis

  fun getBit(): Int? = cmap.indexOf(this).takeIf { it >= 0 }

  fun getMask() = getBit()?.let { 1u shl it } ?: 0u

  companion object {
    fun fromBit(i: Int): WireConnection? = i.takeIf { it in cmap.indices }?.let { cmap[it] }

    @Suppress("NAME_SHADOWING")
    fun fromMask(i: UInt): Set<WireConnection> {
      return i.bits().mapNotNull { fromBit(it) }.toSet()
    }
  }

}

private fun UInt.bits(): Set<Int> {
  var i = this

  val set = mutableSetOf<Int>()
  var b = 0

  while (i != 0u) {
    if (i and 1u != 0u) set += b
    i = i shr 1
    b++
  }

  return set
}

private fun Int.bits() = toUInt().bits()

private val cmap = Direction.values()
  .flatMap { f1 -> Direction.values().map { f2 -> WireConnection(f1, f2) } }
  .filter { it.isValid }

private val ItemUsageContext.hitResult: BlockHitResult
  get() = BlockHitResult(pos, facing, blockPos, method_17699())