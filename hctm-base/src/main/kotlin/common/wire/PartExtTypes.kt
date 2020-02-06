package net.dblsaiko.hctm.common.wire

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

interface WirePartExtType : PartExt {
  val side: Direction

  fun canConnectAt(world: BlockView, pos: BlockPos, edge: Direction): Boolean = true
}

interface FullBlockPartExtType : PartExt {

}