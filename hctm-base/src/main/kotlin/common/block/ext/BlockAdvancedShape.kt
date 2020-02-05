package net.dblsaiko.hctm.common.block.ext

import net.minecraft.block.BlockState
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

interface BlockAdvancedShape {

  fun rayTrace(state: BlockState, pos: BlockPos, from: Vec3d, to: Vec3d): BlockHitResult?

}