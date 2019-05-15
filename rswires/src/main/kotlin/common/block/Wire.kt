package therealfarfetchd.rswires.common.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateFactory.Builder
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import therealfarfetchd.hctm.common.block.BaseWireBlock
import therealfarfetchd.hctm.common.block.BaseWireBlockEntity
import therealfarfetchd.hctm.common.block.WireUtils
import therealfarfetchd.hctm.common.wire.ConnectionHandlers
import therealfarfetchd.hctm.common.wire.Constraints
import therealfarfetchd.hctm.common.wire.NetNode
import therealfarfetchd.hctm.common.wire.NodeView
import therealfarfetchd.hctm.common.wire.PartExt
import therealfarfetchd.hctm.common.wire.WirePartExtType
import therealfarfetchd.rswires.common.init.BlockEntityTypes

class RedAlloyWireBlock : BaseWireBlock(Block.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f), 2 / 16f) {

  override fun appendProperties(b: Builder<Block, BlockState>) {
    super.appendProperties(b)
    b.with(WireProperties.Powered)
  }

  override fun createPartExtFromSide(side: Direction) = RedAlloyWirePartExt(side)

  override fun createBlockEntity(view: BlockView) = BaseWireBlockEntity(BlockEntityTypes.RedAlloyWire)

}

class InsulatedWireBlock(val color: DyeColor) : BaseWireBlock(Block.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f), 3 / 16f) {

  override fun appendProperties(b: Builder<Block, BlockState>) {
    super.appendProperties(b)
    b.with(WireProperties.Powered)
  }

  override fun createPartExtFromSide(side: Direction) = InsulatedWirePartExt(side, color)

  override fun createBlockEntity(view: BlockView) = BaseWireBlockEntity(BlockEntityTypes.InsulatedWire)

}

class BundledCableBlock(val color: DyeColor?) : BaseWireBlock(Block.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f), 4 / 16f) {

  override fun createPartExtFromSide(side: Direction) = BundledCablePartExt(side, color)

  override fun createBlockEntity(view: BlockView) = BaseWireBlockEntity(BlockEntityTypes.BundledCable)

}

data class RedAlloyWirePartExt(override val side: Direction) : PartExt, WirePartExtType, PartRedstoneCarrier {
  override val type = RedstoneWireType.RedAlloy

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return ConnectionHandlers.Wire.tryConnect(self, world, pos, nv, Constraints(PartRedstoneCarrier::class))
  }

  override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
    WireUtils.updateClient(world, pos)
  }

  override fun toTag(): Tag {
    return ByteTag(side.id.toByte())
  }
}

data class InsulatedWirePartExt(override val side: Direction, val color: DyeColor) : PartExt, WirePartExtType, PartRedstoneCarrier {
  override val type = RedstoneWireType.Colored(color)

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return ConnectionHandlers.Wire.tryConnect(self, world, pos, nv, Constraints(PartRedstoneCarrier::class))
  }

  override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
    WireUtils.updateClient(world, pos)
  }

  override fun toTag(): Tag {
    return ByteTag(side.id.toByte())
  }
}

data class BundledCablePartExt(override val side: Direction, val color: DyeColor?) : PartExt, WirePartExtType, PartRedstoneCarrier {
  override val type = RedstoneWireType.Bundled(color)

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return ConnectionHandlers.Wire.tryConnect(self, world, pos, nv, Constraints(PartRedstoneCarrier::class))
  }

  override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
    WireUtils.updateClient(world, pos)
  }

  override fun toTag(): Tag {
    return ByteTag(side.id.toByte())
  }
}

interface PartRedstoneCarrier : PartExt {
  val type: RedstoneWireType
}

sealed class RedstoneWireType {
  object RedAlloy : RedstoneWireType()
  data class Colored(val color: DyeColor) : RedstoneWireType()
  data class Bundled(val color: DyeColor?) : RedstoneWireType()

  fun canConnect(other: RedstoneWireType): Boolean {
    if (this == other) return true
    if (this == RedAlloy && other is Colored || this is Colored && other == RedAlloy) return true
    if (this is Colored && other is Bundled || this is Bundled && other is Colored) return true
    if (this == Bundled(null) && other is Bundled || this is Bundled && other == Bundled(null)) return true
    return false
  }
}

object WireProperties {
  val Powered = Properties.POWERED
}