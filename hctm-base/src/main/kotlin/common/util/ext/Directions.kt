package net.dblsaiko.hctm.common.util.ext

import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis.X
import net.minecraft.util.math.Direction.Axis.Y
import net.minecraft.util.math.Direction.Axis.Z
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.util.math.Direction.EAST
import net.minecraft.util.math.Direction.NORTH
import net.minecraft.util.math.Direction.SOUTH
import net.minecraft.util.math.Direction.UP
import net.minecraft.util.math.Direction.WEST

fun Direction.rotateClockwise(axis: Direction.Axis): Direction {
  return when (axis) {
    X -> when (this) {
      DOWN -> SOUTH
      UP -> NORTH
      NORTH -> DOWN
      SOUTH -> UP
      else -> this
    }
    Y -> when (this) {
      NORTH -> EAST
      SOUTH -> WEST
      WEST -> NORTH
      EAST -> SOUTH
      else -> this
    }
    Z -> when (this) {
      DOWN -> WEST
      UP -> EAST
      WEST -> UP
      EAST -> DOWN
      else -> this
    }
  }
}