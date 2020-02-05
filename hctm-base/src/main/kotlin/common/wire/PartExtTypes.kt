package net.dblsaiko.hctm.common.wire

import net.minecraft.util.math.Direction

interface WirePartExtType : PartExt {
  val side: Direction
}

interface FullBlockPartExtType : PartExt {

}