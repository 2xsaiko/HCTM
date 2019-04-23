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
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateFactory.Builder
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.NORTH
import net.minecraft.world.IWorld
import net.minecraft.world.World
import therealfarfetchd.retrocomputers.common.block.wire.BlockPartProvider
import therealfarfetchd.retrocomputers.common.block.wire.ConnectionHandlers
import therealfarfetchd.retrocomputers.common.block.wire.NetNode
import therealfarfetchd.retrocomputers.common.block.wire.NodeView
import therealfarfetchd.retrocomputers.common.block.wire.PartExt
import therealfarfetchd.retrocomputers.common.block.wire.getWireNetworkState

abstract class BaseBlock : BlockWithEntity(Block.Settings.of(Material.METAL)), BlockPartProvider {

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

  override fun method_9517(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
    if (!world.isClient && world is ServerWorld)
      world.getWireNetworkState().controller.onBlockChanged(world, pos, state)
  }

  override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt<out Any?>> {
    return setOf(MachinePartExt)
  }

  override fun createExtFromTag(tag: Tag): PartExt<out Any?>? {
    return MachinePartExt
  }

  companion object {
    val Direction = HorizontalFacingBlock.FACING
  }

}

object MachinePartExt : PartExt<Nothing?> {

  override val data: Nothing?
    get() = null

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return ConnectionHandlers.StandardMachine.tryConnect(self, world, pos, nv)
  }

  override fun toTag(): Tag = ByteTag(0)

  override fun hashCode(): Int = super.hashCode()

  override fun equals(other: Any?): Boolean = super.equals(other)
}

abstract class BaseBlockEntity(type: BlockEntityType<*>) : BlockEntity(type)