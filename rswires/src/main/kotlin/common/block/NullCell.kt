package net.dblsaiko.rswires.common.block

import net.dblsaiko.hctm.common.block.WireUtils
import net.dblsaiko.hctm.common.wire.ConnectionDiscoverers
import net.dblsaiko.hctm.common.wire.ConnectionFilter
import net.dblsaiko.hctm.common.wire.NetNode
import net.dblsaiko.hctm.common.wire.NodeView
import net.dblsaiko.hctm.common.wire.PartExt
import net.dblsaiko.hctm.common.wire.WirePartExtType
import net.dblsaiko.hctm.common.wire.find
import net.dblsaiko.rswires.common.util.adjustRotation
import net.dblsaiko.rswires.common.util.rotate
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityContext
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager.Builder
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class NullCellBlock(settings: Block.Settings) : GateBlock(settings) {

  init {
    defaultState = defaultState
      .with(NullCellProperties.BOTTOM_POWERED, false)
      .with(NullCellProperties.TOP_POWERED, false)
  }

  override fun appendProperties(builder: Builder<Block, BlockState>) {
    super.appendProperties(builder)
    builder.add(NullCellProperties.BOTTOM_POWERED)
    builder.add(NullCellProperties.TOP_POWERED)
  }

  override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt> {
    val side = getSide(state)
    val rotation = state[GateProperties.ROTATION]
    return setOf(NullCellPartExt(side, rotation, false), NullCellPartExt(side, rotation, true))
  }

  override fun createExtFromTag(tag: Tag): PartExt? {
    val data = (tag as? ByteTag)?.int ?: return null
    val top = data and 1 != 0
    val side = Direction.byId(data shr 1 and 0b111)
    val rotation = data shr 4
    return NullCellPartExt(side, rotation, top)
  }

  override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: EntityContext): VoxelShape {
    return SELECTION_BOXES.getValue(state[Properties.FACING])
  }

  override fun getCullingShape(state: BlockState, view: BlockView, pos: BlockPos): VoxelShape {
    return CULL_BOX.getValue(state[Properties.FACING])[state[GateProperties.ROTATION]]
  }

  companion object {
    val SELECTION_BOXES = WireUtils.generateShapes(12 / 16.0)

    val CULL_BOX = VoxelShapes.union(
      VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 2 / 16.0, 1.0),
      VoxelShapes.cuboid(7 / 16.0, 0.0, 0.0, 9 / 16.0, 12 / 16.0, 1.0),
      VoxelShapes.cuboid(0.0, 0.0, 7 / 16.0, 1.0, 3 / 16.0, 9 / 16.0)
    ).let { box ->
      Direction.values().asIterable().associateWith { face -> Array(4) { rotation -> box.rotate(face, rotation) } }
    }.let(::EnumMap)
  }

}

object NullCellProperties {
  val BOTTOM_POWERED = BooleanProperty.of("bottom_powered")
  val TOP_POWERED = BooleanProperty.of("top_powered")
}

data class NullCellPartExt(override val side: Direction, val rotation: Int, val top: Boolean) : PartExt, WirePartExtType, PartRedstoneCarrier {
  override val type = RedstoneWireType.RedAlloy

  override fun getState(world: World, self: NetNode): Boolean {
    val pos = self.data.pos
    val prop = if (top) NullCellProperties.TOP_POWERED else NullCellProperties.BOTTOM_POWERED
    return world.getBlockState(pos)[prop]
  }

  override fun setState(world: World, self: NetNode, state: Boolean) {
    val pos = self.data.pos
    val prop = if (top) NullCellProperties.TOP_POWERED else NullCellProperties.BOTTOM_POWERED
    world.setBlockState(pos, world.getBlockState(pos).with(prop, state))
  }

  override fun getInput(world: World, self: NetNode): Boolean {
    return false
  }

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    val axis = adjustRotation(side, rotation, if (top) 1 else 0).axis
    return find(ConnectionDiscoverers.WIRE, RedstoneCarrierFilter and ConnectionFilter { self, other ->
      self.data.pos.subtract(other.data.pos)
        .let { Direction.fromVector(it.x, it.y, it.z) }
        ?.let { it.axis == axis }
      ?: false
    }, self, world, pos, nv)
  }

  override fun canConnectAt(world: BlockView, pos: BlockPos, edge: Direction): Boolean {
    val axis = adjustRotation(side, rotation, if (top) 1 else 0).axis
    return edge.axis == axis
  }

  override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
    RedstoneWireUtils.scheduleUpdate(world, pos)
  }

  override fun toTag(): Tag {
    return ByteTag.of(((if (top) 1 else 0) or (side.id shl 1) or (rotation shl 4)).toByte())
  }
}