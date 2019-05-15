package therealfarfetchd.hctm.common.block

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.advancement.criterion.Criterions
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.WallMountedBlock
import net.minecraft.entity.EntityContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
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
import therealfarfetchd.hctm.common.block.ConnectionType.CORNER
import therealfarfetchd.hctm.common.block.ConnectionType.EXTERNAL
import therealfarfetchd.hctm.common.block.ConnectionType.INTERNAL
import therealfarfetchd.hctm.common.block.ext.BlockCustomBreak
import therealfarfetchd.hctm.common.wire.BlockPartProvider
import therealfarfetchd.hctm.common.wire.PartExt
import therealfarfetchd.hctm.common.wire.WirePartExtType
import therealfarfetchd.hctm.common.wire.getWireNetworkState
import net.minecraft.block.Blocks as MCBlocks

abstract class BaseWireBlock(settings: Block.Settings, val height: Float) : Block(settings), BlockCustomBreak, BlockPartProvider, BlockEntityProvider {

  init {
    defaultState =
      BaseWireProperties.PlacedWires.values.fold(defaultState) { state, prop -> state.with(prop, false) }
  }

  override fun appendProperties(b: Builder<Block, BlockState>) {
    for (prop in BaseWireProperties.PlacedWires.values) {
      b.add(prop)
    }
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
    return super.getPlacementState(ctx)?.with(BaseWireProperties.PlacedWires.getValue(ctx.facing.opposite), true)
  }

  override fun canPlaceAt(state: BlockState, world: ViewableWorld, pos: BlockPos): Boolean {
    return WireUtils.getOccupiedSides(state).all { side -> WallMountedBlock.canPlaceAt(world, pos, side) }
  }

  private fun getShape(state: BlockState): VoxelShape {
    return BaseWireProperties.PlacedWires.entries
      .filter { (_, prop) -> state[prop] }
      .map { (a, _) -> WireUtils.getShapeForSide(height.toDouble(), a) }
      .fold(VoxelShapes.empty(), VoxelShapes::union)
  }

  override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ec: EntityContext?): VoxelShape {
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
    if (!state[BaseWireProperties.PlacedWires.getValue(side)]) return false

    val result = world.setBlockState(pos, state.with(BaseWireProperties.PlacedWires.getValue(side), false))

    if (result) {
      onBreak(world, pos, getStateForSide(side), player) // particle
      // dropStack(world, pos, Items.Wire.defaultStack) FIXME
    }

    return result
  }

  override fun method_9517(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
    if (!world.isClient && world is ServerWorld)
      world.getWireNetworkState().controller.onBlockChanged(world, pos, state)
  }

  override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt> {
    return WireUtils.getOccupiedSides(state).map(::createPartExtFromSide).toSet()
  }

  override fun createExtFromTag(tag: Tag): PartExt? {
    return (tag as? ByteTag)
      ?.takeIf { it.int in 0 until 6 }
      ?.let { createPartExtFromSide(Direction.byId(it.int)) }
  }

  protected abstract fun createPartExtFromSide(side: Direction): PartExt

  private fun getStateForSide(vararg side: Direction): BlockState =
    if (side.isEmpty()) MCBlocks.AIR.defaultState else
      side.fold(defaultState) { state, s -> state.with(BaseWireProperties.PlacedWires.getValue(s), true) }

  override fun getStateForNeighborUpdate(state: BlockState, side: Direction, state1: BlockState, world: IWorld, pos: BlockPos, pos1: BlockPos): BlockState {
    return getStateForSide(*WireUtils.getOccupiedSides(state).filter { it != side || getStateForSide(it).canPlaceAt(world, pos) }.toTypedArray())
  }

}

open class BaseWireBlockEntity(type: BlockEntityType<out BlockEntity>) : BlockEntity(type), BlockEntityClientSerializable {

  var connections: Set<WireRepr> = emptySet()
    private set

  override fun toTag(tag: CompoundTag): CompoundTag {
    tag.putLong("c", serConnections())
    return super.toTag(tag)
  }

  override fun fromTag(tag: CompoundTag) {
    super.fromTag(tag)
    deserConnections(tag.getLong("c"))
  }

  override fun toClientTag(tag: CompoundTag): CompoundTag {
    tag.putLong("c", serConnections())
    return tag
  }

  override fun fromClientTag(tag: CompoundTag) {
    deserConnections(tag.getLong("c"))
    getWorld()?.updateListeners(getPos(), cachedState, cachedState, 3)
  }

  // data structure:
  // 2 bits for ConnectionType
  // 4 ConnectionTypes + 1 "exists" bit -> 9 bits per face
  // 6 faces -> 54 bits

  private fun serConnections(): Long {
    var l = 0L
    for (c in connections) {
      val list = Direction.values().filter { it.axis != c.side.axis }
      val pos = 9 * c.side.id
      l = 1L shl pos or l
      for (s in c.connections) {
        val pos = 9 * c.side.id + 2 * list.indexOf(s.edge) + 1
        val bits = when (s.type) {
          INTERNAL -> 1L; EXTERNAL -> 2L; CORNER -> 3L
        }
        l = bits shl pos or l
      }
    }
    return l
  }

  private fun deserConnections(packed: Long) {
    connections = emptySet()
    for (face in Direction.values()) {
      if (packed shr (9 * face.id) and 1L != 0L) {
        val list = mutableSetOf<Connection>()
        for ((i, edge) in Direction.values().filter { it.axis != face.axis }.withIndex()) {
          val type = when (packed shr (9 * face.id + 2 * i + 1) and 3L) {
            1L -> INTERNAL; 2L -> EXTERNAL; 3L -> CORNER; else -> null
          }
          if (type != null) list += Connection(edge, type)
        }
        connections += WireRepr(face, list)
      }
    }
  }

  fun updateConnections(connections: Set<WireRepr>) {
    if (connections != this.connections) {
      this.connections = connections
      markDirty()
      getWorld()?.updateListeners(getPos(), cachedState, cachedState, 3)
    }
  }

}

open class BaseWireItem(block: BaseWireBlock, settings: Item.Settings = Item.Settings()) : BlockItem(block, settings) {

  override fun place(ctx: ItemPlacementContext): ActionResult {
    val state = this.getBlockState(ctx) ?: return ActionResult.PASS

    if (!doPlace(ctx, state)) return ActionResult.PASS

    return ActionResult.SUCCESS
  }

  private fun tryFit(state: BlockState, new: BlockState): BlockState? {
    if (state.block != block || state.block != new.block) return null
    val v1 = WireUtils.getOccupiedSides(state)
    val v2 = WireUtils.getOccupiedSides(new)
    if ((v1 + v2).size != v1.size + v2.size) return null
    return v2.fold(state) { s, side -> s.with(BaseWireProperties.PlacedWires.getValue(side), true) }
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
    world.playSound(player, pos, this.getPlaceSound(placedState), SoundCategory.BLOCKS, (sg.getVolume() + 1.0f) / 2.0f, sg.getPitch() * 0.8f)
    stack.subtractAmount(1)
    return true
  }

}

object BaseWireProperties {
  val PlacedWires = mapOf(
    UP to Properties.UP_BOOL,
    DOWN to Properties.DOWN_BOOL,
    NORTH to Properties.NORTH_BOOL,
    SOUTH to Properties.SOUTH_BOOL,
    EAST to Properties.EAST_BOOL,
    WEST to Properties.WEST_BOOL
  )
}

data class WireRepr(val side: Direction, val connections: Set<Connection>)

data class Connection(val edge: Direction, val type: ConnectionType)

enum class ConnectionType {
  INTERNAL,
  EXTERNAL,
  CORNER,
}

object WireUtils {
  @Suppress("UNCHECKED_CAST")
  fun rayTrace(state: BlockState, pos: BlockPos, from: Vec3d, to: Vec3d): Pair<Direction, BlockHitResult>? {
    val block = state.block as BaseWireBlock

    return BaseWireProperties.PlacedWires.entries.asSequence()
      .filter { (_, prop) -> state[prop] }
      .map { (a, _) -> Pair(a, getShapeForSide(block.height.toDouble(), a)) }
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
    return BaseWireProperties.PlacedWires.entries
      .filter { (_, prop) -> state[prop] }
      .map { it.key }
      .toSet()
  }

  fun updateClient(world: ServerWorld, pos: BlockPos) {
    val be = world.getBlockEntity(pos) as? BaseWireBlockEntity ?: return
    val state = world.getBlockState(pos)
    val net = world.getWireNetworkState().controller
    val nodes = net.getNodesAt(pos)
    val connMap: Map<Direction?, List<Connection>?> = nodes.associate { node ->
      val side = (node.data.ext as? WirePartExtType)?.side ?: return@associate Pair(null, null)
      side to node.connections.mapNotNull {
        val other = it.other(node)
        if (node.data.pos == other.data.pos && other.data.ext is WirePartExtType) Connection(other.data.ext.side, INTERNAL)
        else other.data.pos.subtract(node.data.pos.offset(side)).let { Direction.fromVector(it.x, it.y, it.z) }?.let { Connection(it, CORNER) }
             ?: other.data.pos.subtract(node.data.pos).let { Direction.fromVector(it.x, it.y, it.z) }?.let { Connection(it, EXTERNAL) }
      }
    }
    val connections = getOccupiedSides(state).map { side -> WireRepr(side, connMap[side]?.toSet().orEmpty()) }.toSet()
    be.updateConnections(connections)
  }

}