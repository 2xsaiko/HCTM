package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateFactory.Builder
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.NORTH

abstract class BaseBlock : BlockWithEntity(Block.Settings.of(Material.METAL)) {

  init {
    this.defaultState = this.stateFactory.defaultState.with(Direction, NORTH)
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
    return this.defaultState.with(Direction, ctx.playerHorizontalFacing.opposite)
  }

  override fun getRenderType(p0: BlockState?) = MODEL

  override fun rotate(state: BlockState, rotation: Rotation): BlockState =
    state.with(Direction, rotation.rotate(state.get<Direction>(Direction) as Direction))

  override fun mirror(state: BlockState, mirror: Mirror): BlockState =
    state.rotate(mirror.getRotation(state.get<Direction>(Direction) as Direction))

  override fun appendProperties(b: Builder<Block, BlockState>) {
    b.with(Direction)
  }

  companion object {
    val Direction = HorizontalFacingBlock.FACING
  }

}

abstract class BaseBlockEntity(type: BlockEntityType<*>) : BlockEntity(type)