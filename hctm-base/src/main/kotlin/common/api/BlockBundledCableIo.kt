package net.dblsaiko.hctm.common.api

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

/**
 * An interface to make a block be able to connect to an RSWires bundled cable.
 */
interface BlockBundledCableIo {

  @JvmDefault
  fun canBundledConnectTo(state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction): Boolean = false

  @JvmDefault
  fun getBundledOutput(state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction): UShort = 0u

  @JvmDefault
  fun onBundledInputChange(data: UShort, state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction) {
  }

}