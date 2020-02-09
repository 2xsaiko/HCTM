package net.dblsaiko.rswires.common.block

import net.dblsaiko.hctm.common.block.WireUtils
import net.dblsaiko.hctm.common.wire.BlockPartProvider
import net.dblsaiko.hctm.common.wire.getWireNetworkState
import net.dblsaiko.rswires.common.util.reverseAdjustRotation
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager.Builder
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis.*
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld

abstract class GateBlock(settings: Block.Settings) : Block(settings), BlockPartProvider {

  override fun appendProperties(builder: Builder<Block, BlockState>) {
    super.appendProperties(builder)
    builder.add(Properties.FACING)
    builder.add(GateProperties.Rotation)
  }

  override fun method_9517(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
    if (!world.isClient && world is ServerWorld)
      world.getWireNetworkState().controller.onBlockChanged(world, pos, state)
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
    val facing = ctx.side.opposite
    val rotation = ctx.player?.rotationVector?.let {
      val axis = facing.axis
      val edge = Direction.getFacing(
        if (axis == X) 0.0 else it.x,
        if (axis == Y) 0.0 else it.y,
        if (axis == Z) 0.0 else it.z
      )
      reverseAdjustRotation(facing, edge)
    } ?: 0
    return defaultState
      .with(Properties.FACING, facing)
      .with(GateProperties.Rotation, rotation)
  }

  fun getSide(state: BlockState) = state[Properties.FACING]

  override fun getStateForNeighborUpdate(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
    return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
  }

  override fun calcBlockBreakingDelta(state: BlockState, player: PlayerEntity, world: BlockView, pos: BlockPos): Float {
    val f = state.getHardness(world, pos)
    return if (f == -1.0f) {
      0.0f
    } else {
      1.0f / f / 100.0f
    }
  }

  override fun getCollisionShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: EntityContext): VoxelShape {
    return Collision.getValue(state[Properties.FACING])
  }

  override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: EntityContext): VoxelShape {
    return Collision.getValue(state[Properties.FACING])
  }

  companion object {
    val Collision = WireUtils.generateShapes(2 / 16.0)
  }

}

object GateProperties {

  val Rotation = IntProperty.of("rotation", 0, 3)

}