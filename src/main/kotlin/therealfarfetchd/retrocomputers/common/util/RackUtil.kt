package therealfarfetchd.retrocomputers.common.util

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import therealfarfetchd.quacklib.common.api.extensions.minus
import therealfarfetchd.quacklib.common.api.extensions.plus
import therealfarfetchd.retrocomputers.common.block.Rack
import therealfarfetchd.retrocomputers.common.block.RackExt

fun getRackPos(world: IBlockAccess, pos: BlockPos): BlockPos? {
  val te = world.getTileEntity(pos)
  return when (te) {
    is Rack.Tile    -> pos
    is RackExt.Tile -> pos + te.offset
    else            -> null
  }
}

fun checkIntegrity(world: IBlockAccess, pos: BlockPos): Boolean {
  val rackPos = getRackPos(world, pos) ?: return false
  val rackTile = world.getTileEntity(rackPos) as? Rack.Tile ?: return false
  val height = rackTile.heightBlocks
  return getBounds(rackPos, height).all { getRackPos(world, it) == rackPos }
}

fun canPlaceAt(world: IBlockAccess, pos: BlockPos, height: Int) =
  getBounds(pos, height).all { world.getBlockState(pos).block.isReplaceable(world, it) }

fun placeRackExt(world: World, pos: BlockPos) {
  val rackTile = world.getTileEntity(pos) as Rack.Tile
  getBounds(pos, rackTile.heightBlocks)
    .filterNot { pos == it }
    .forEach {
      world.setBlockState(it, RackExt.Block.defaultState)
      (world.getTileEntity(it) as RackExt.Tile).offset = pos - it
    }
}

fun getBounds(pos: BlockPos, height: Int): Iterable<BlockPos> = BlockPos.getAllInBox(pos, pos.up(height - 1))