package net.dblsaiko.hctm.client.render.model

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import org.joml.Vector4fc

fun Vector2fc.toMutable(): Vector2f = Vector2f(x(), y())

fun Vector2f.toImmutable(): Vector2fc = toMutable()

private fun Vector2f.toImmutableUnsafe(): Vector2fc = this

fun mutVec2(x: Float, y: Float): Vector2f = Vector2f(x, y)
fun vec2(x: Float, y: Float): Vector2fc = mutVec2(x, y)

fun mutVec3(x: Float, y: Float, z: Float): Vector3f = Vector3f(x, y, z)
fun vec3(x: Float, y: Float, z: Float): Vector3fc = mutVec3(x, y, z)

fun mutVec4(x: Float, y: Float, z: Float, w: Float): Vector4f = Vector4f(x, y, z, w)
fun vec4(x: Float, y: Float, z: Float, w: Float): Vector4fc = mutVec4(x, y, z, w)

operator fun Vector2fc.plus(other: Vector2fc): Vector2fc = toMutable().add(other)

operator fun Vector2fc.minus(other: Vector2fc): Vector2fc = toMutable().sub(other)

operator fun Vector2fc.times(other: Vector2fc): Vector2fc = toMutable().mul(other)

operator fun Vector2fc.div(other: Vector2fc): Vector2fc = toMutable().mul(1 / other.x(), 1 / other.y())

operator fun Vector2fc.times(other: Float): Vector2fc = toMutable().mul(other)

operator fun Vector2fc.div(other: Float): Vector2fc = toMutable().mul(1 / other)

operator fun Vector2f.plusAssign(other: Vector2fc) {
  add(other)
}

operator fun Vector2f.minusAssign(other: Vector2fc) {
  sub(other)
}

operator fun Vector2f.timesAssign(other: Vector2fc) {
  mul(other)
}

operator fun Vector2f.divAssign(other: Vector2fc) {
  mul(1 / other.x(), 1 / other.y())
}

operator fun Vector2f.timesAssign(other: Float) {
  mul(other)
}

operator fun Vector2f.divAssign(other: Float) {
  mul(1 / other)
}