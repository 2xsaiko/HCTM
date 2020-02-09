package net.dblsaiko.rswires.common.util

import net.dblsaiko.hctm.common.util.ext.rotateClockwise
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import therealfarfetchd.qcommon.croco.Mat4
import therealfarfetchd.qcommon.croco.Vec3
import java.util.*

/**
 * Convert block face + rotation to a block edge (side is implied).
 */
fun adjustRotation(side: Direction, rotation: Int, targetOut: Int): Direction {
  var start = when (side.axis) {
    Direction.Axis.X, Direction.Axis.Z -> Direction.DOWN
    Direction.Axis.Y -> Direction.WEST
  }

  repeat((rotation + targetOut) % 4) {
    start = start.rotateClockwise(side.axis)
  }

  return start
}

/**
 * Convert block edge to rotation from 0 to 4 on the specified face.
 */
fun reverseAdjustRotation(side: Direction, edge: Direction): Int {
  var start = when (side.axis) {
    Direction.Axis.X, Direction.Axis.Z -> Direction.DOWN
    Direction.Axis.Y -> Direction.WEST
  }

  var r = 0

  while (start != edge) {
    start = start.rotateClockwise(side.axis)
    r += 1
  }

  return r
}

private val matrixMap = Direction.values().asIterable().associateWith { face ->
  Array(4) { rotation ->
    val matrix = getRotationFor0(face, rotation)
    val rotMatrix = matrix.toArray().also { it[3] = 0.0f; it[7] = 0.0f; it[11] = 0.0f }.let { Mat4.fromArray(it) }
    Matrices(matrix, rotMatrix)
  }
}.let(::EnumMap)

data class Matrices(val matrix: Mat4, val normal: Mat4)

/**
 * Get a matrix and rotation-only matrix converting space of face=DOWN, rotation=0 to the specified rotated space
 */
fun getRotationFor(face: Direction, rotation: Int) =
  matrixMap.getValue(face)[rotation]

private fun getRotationFor0(face: Direction, rotation: Int): Mat4 {
  val m1 = Mat4.IDENTITY
    .translate(0.5f, 0.5f, 0.5f)

  return when (face) {
    Direction.DOWN -> m1
    Direction.UP -> m1.rotate(1.0f, 0.0f, 0.0f, 180.0f).rotate(0.0f, 1.0f, 0.0f, 180.0f)
    Direction.NORTH -> m1.rotate(1.0f, 0.0f, 0.0f, -90.0f).rotate(0.0f, 1.0f, 0.0f, 90.0f)
    Direction.SOUTH -> m1.rotate(0.0f, 1.0f, 0.0f, 180.0f).rotate(1.0f, 0.0f, 0.0f, -90.0f).rotate(0.0f, 1.0f, 0.0f, 90.0f)
    Direction.WEST -> m1.rotate(0.0f, 0.0f, 1.0f, 90.0f)
    Direction.EAST -> m1.rotate(0.0f, 1.0f, 0.0f, 180.0f).rotate(0.0f, 0.0f, 1.0f, 90.0f)
    else -> m1
  }
    .rotate(0.0f, 1.0f, 0.0f, rotation * 90.0f)
    .translate(-0.5f, -0.5f, -0.5f)
}

fun Box.transform(mat: Mat4): Box {
  return Box(
    mat.mul(Vec3(x1.toFloat(), y1.toFloat(), z1.toFloat())).toVec3d(),
    mat.mul(Vec3(x2.toFloat(), y2.toFloat(), z2.toFloat())).toVec3d()
  )
}

fun Box.rotate(x: Float, y: Float, z: Float, angle: Float) =
  this.transform(Mat4.IDENTITY.rotate(x, y, z, angle))

fun Box.rotate(face: Direction, rotation: Int): Box {
  val (mat, _) = getRotationFor(face, rotation)
  return this.transform(mat)
}

fun VoxelShape.rotate(face: Direction, rotation: Int): VoxelShape {
  return this.boundingBoxes
    .map { it.rotate(face, rotation) }
    .map(VoxelShapes::cuboid)
    .fold(VoxelShapes.empty(), VoxelShapes::union)
}