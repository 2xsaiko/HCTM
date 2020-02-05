package net.dblsaiko.hctm.client.render.model

import net.dblsaiko.hctm.client.render.model.CenterVariant.Crossing
import net.dblsaiko.hctm.client.render.model.CenterVariant.Standalone
import net.dblsaiko.hctm.client.render.model.CenterVariant.Straight1
import net.dblsaiko.hctm.client.render.model.CenterVariant.Straight2
import net.dblsaiko.hctm.client.render.model.ExtVariant.Corner
import net.dblsaiko.hctm.client.render.model.ExtVariant.External
import net.dblsaiko.hctm.client.render.model.ExtVariant.Internal
import net.dblsaiko.hctm.client.render.model.ExtVariant.Terminal
import net.dblsaiko.hctm.client.render.model.ExtVariant.Unconnected
import net.dblsaiko.hctm.client.render.model.ExtVariant.UnconnectedCrossing
import net.dblsaiko.hctm.common.util.ext.rotateClockwise
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Direction.Axis.X
import net.minecraft.util.math.Direction.Axis.Y
import net.minecraft.util.math.Direction.Axis.Z
import net.minecraft.util.math.Direction.AxisDirection
import net.minecraft.util.math.Direction.AxisDirection.NEGATIVE
import net.minecraft.util.math.Direction.AxisDirection.POSITIVE
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.util.math.Direction.EAST
import net.minecraft.util.math.Direction.NORTH
import net.minecraft.util.math.Direction.SOUTH
import net.minecraft.util.math.Direction.UP
import net.minecraft.util.math.Direction.WEST
import net.minecraft.util.math.Vec2f
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3fc
import java.util.function.Function
import kotlin.math.PI

class UnbakedWireModel(
  val texture: Identifier,
  val cableWidth: Float,
  val cableHeight: Float,
  textureSize: Float
) : UnbakedModel {

  private val armLength: Float = (1 - cableWidth) / 2
  private val armInnerLength: Float = armLength - cableHeight
  private val armInnerSp: Float = armLength - armInnerLength
  private val scaleFactor: Float = textureSize / 16F

  // texture positions
  private val arm1TopUv = vec2(0.0f, 0.0f)
  private val arm2TopUv = vec2(0.0f, armLength + cableWidth) / scaleFactor
  private val centerTopUv = vec2(0.0f, armLength) / scaleFactor
  private val centerTopCUv = vec2(0.0f, 1.0f) / scaleFactor
  private val arm1Side1Uv = vec2(cableWidth, 0.0f) / scaleFactor
  private val arm2Side1Uv = vec2(cableWidth, armLength + cableWidth) / scaleFactor
  private val centerSide1Uv = vec2(cableWidth, armLength) / scaleFactor
  private val arm1Side2Uv = vec2(cableWidth + cableHeight, 0.0f) / scaleFactor
  private val arm2Side2Uv = vec2(cableWidth + cableHeight, armLength + cableWidth) / scaleFactor
  private val centerSide2Uv = vec2(cableWidth + cableHeight, armLength) / scaleFactor
  private val arm1BottomUv = vec2(cableWidth + 2 * cableHeight, 0.0f) / scaleFactor
  private val arm2BottomUv = vec2(cableWidth + 2 * cableHeight, armLength + cableWidth) / scaleFactor
  private val centerBottomUv = vec2(cableWidth + 2 * cableHeight, armLength) / scaleFactor
  private val cableFrontUv = vec2(cableWidth, 1.0f) / scaleFactor
  private val cableBackUv = vec2(cableWidth + cableHeight, 1.0f) / scaleFactor
  private val cornerTop1Uv = vec2(0.0f, 1.0f + cableWidth) / scaleFactor
  private val cornerTop2Uv = vec2(cableWidth + 2 * cableHeight, 1.0f + cableWidth) / scaleFactor
  private val cornerSide1Uv = vec2(cableWidth, 1.0f + cableWidth) / scaleFactor
  private val cornerSide2Uv = vec2(cableWidth + cableHeight, 1.0f + cableWidth) / scaleFactor
  private val icornerSide1Uv = vec2(2 * cableWidth + 2 * cableHeight, 0.0f) / scaleFactor
  private val icornerSide2Uv = vec2(2 * cableWidth + 2 * cableHeight, cableHeight) / scaleFactor
  private val center8Top1Uv = vec2(0.0f, 0.25f) / scaleFactor
  private val center8Top2Uv = arm2TopUv
  private val center8Bottom1Uv = vec2(cableWidth + 2 * cableHeight, 0.25f) / scaleFactor
  private val center8Bottom2Uv = arm2BottomUv
  private val center8Arm1Side1Uv = vec2(cableWidth, 0.25f) / scaleFactor
  private val center8Arm1Side2Uv = vec2(cableWidth + cableHeight, 0.25f) / scaleFactor
  private val center8Arm2Side1Uv = arm2Side1Uv
  private val center8Arm2Side2Uv = arm2Side2Uv
  private val innerTop1Uv = vec2(0.0f, armInnerSp) / scaleFactor
  private val innerTop2Uv = arm2TopUv
  private val innerBottom1Uv = vec2(cableWidth + 2 * cableHeight, armInnerSp) / scaleFactor
  private val innerBottom2Uv = arm2BottomUv
  private val innerArm1Side1Uv = vec2(cableWidth, armInnerSp) / scaleFactor
  private val innerArm1Side2Uv = vec2(cableWidth + cableHeight, armInnerSp) / scaleFactor
  private val innerArm2Side1Uv = arm2Side1Uv
  private val innerArm2Side2Uv = arm2Side2Uv

  val renderer = RendererAccess.INSTANCE.renderer
  val builder = renderer.meshBuilder()
  val finder = renderer.materialFinder()

  override fun bake(ml: ModelLoader, getTexture: Function<SpriteIdentifier, Sprite>, settings: ModelBakeSettings, p3: Identifier): BakedModel? {
    finder.clear()
    val standard = finder.find()
    finder.disableAo(0, true)
    val corner = finder.find()

    val sid = SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, texture)
    val parts = generateParts(RenderData(Materials(standard, corner), getTexture.apply(sid)))

    return WireModel(getTexture.apply(sid), parts)
  }

  private fun generateParts(d: RenderData): WireModelParts {
    return WireModelParts(Direction.values().toList().associateWith { generateSide(d, it) })
  }

  private fun generateSide(d: RenderData, side: Direction): WireModelPart {
    val cvs = CenterVariant.values().associate { it to generateCenter(d, side, it) }
    val exts = mutableMapOf<Pair<Direction, ExtVariant>, Mesh>()

    for (ext in Direction.values().filter { it.axis != side.axis }) {
      for (v in ExtVariant.values()) {
        exts[Pair(ext, v)] = generateExt(d, side, ext, v)
      }
    }

    return WireModelPart(cvs, exts)
  }

  private fun generateCenter(d: RenderData, side: Direction, variant: CenterVariant): Mesh {
    val axis = when (variant) {
      Straight1, Standalone, Crossing -> when (side.axis) {
        X -> Z
        Y -> X
        Z -> X
      }
      Straight2 -> when (side.axis) {
        X -> Y
        Y -> Z
        Z -> Y
      }
    }

    val (topUv, bottomUv) = when (variant) {
      Crossing -> Pair(centerTopCUv, centerTopCUv)
      Straight1, Straight2, Standalone -> Pair(centerTopUv, centerBottomUv)
    }

    box(
      vec3(armLength, 0f, armLength),
      vec3(1 - armLength, cableHeight, 1 - armLength),
      down = UvCoords(bottomUv, cableWidth / scaleFactor, cableWidth / scaleFactor),
      up = UvCoords(topUv, cableWidth / scaleFactor, cableWidth / scaleFactor)
    ).transform(getExtGenInfo(side, Direction.from(axis, POSITIVE)).first).into(builder.emitter, d.texture, d.materials.standard)
    return builder.build()
  }

  private fun generateExt(d: RenderData, side: Direction, edge: Direction, variant: ExtVariant): Mesh {
    val baseLength = when (variant) {
      External -> armLength
      Internal -> armInnerLength
      Corner -> armLength
      Unconnected -> 0f
      UnconnectedCrossing -> 0f
      Terminal -> armLength - 0.25f
    }

    val origin = Vec2f(armLength, armLength - baseLength)

    val (mat, dir) = getExtGenInfo(side, edge)

    val swapUnconnectedSides = (side.direction == POSITIVE) xor ((side.axis == Y && edge.axis == X) || (side.axis == X && edge.axis == Y) || (side.axis == Z && edge.axis == Y))

    if (dir == POSITIVE) {
      val uvTop = when (variant) {
        External, Unconnected, UnconnectedCrossing -> arm1TopUv
        Internal -> innerTop1Uv
        Corner -> arm1TopUv
        Terminal -> center8Top1Uv
      }

      val uvBottom = when (variant) {
        External, Unconnected, UnconnectedCrossing -> arm1BottomUv
        Internal -> innerBottom1Uv
        Corner -> arm1BottomUv
        Terminal -> center8Bottom1Uv
      }

      val (uvSide1, uvSide2) = when (variant) {
        External, Corner, Unconnected, UnconnectedCrossing -> Pair(arm1Side1Uv, arm1Side2Uv)
        Internal -> Pair(innerArm1Side1Uv, innerArm1Side2Uv)
        Terminal -> Pair(center8Arm1Side1Uv, center8Arm1Side2Uv)
      }

      val uvFront = cableFrontUv.takeIf { variant in setOf(External, Terminal) }

      box(
        vec3(armLength, 0f, 1 - armLength),
        vec3(1 - armLength, cableHeight, 1 - armLength + baseLength),
        down = UvCoords(uvBottom, cableWidth / scaleFactor, baseLength / scaleFactor),
        up = UvCoords(uvTop, cableWidth / scaleFactor, baseLength / scaleFactor),
        south = uvFront?.let { UvCoords(uvFront, cableHeight / scaleFactor, cableWidth / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U) },
        west = UvCoords(uvSide1, cableHeight / scaleFactor, baseLength / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U),
        east = UvCoords(uvSide2, cableHeight / scaleFactor, baseLength / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U)
      ).transform(mat).into(builder.emitter, d.texture, d.materials.standard)

      when (variant) {
        Internal -> {
          box(
            vec3(armLength, 0f, 1 - cableHeight),
            vec3(1 - armLength, cableHeight, 1f),
            up = UvCoords(cableFrontUv, cableHeight / scaleFactor, cableWidth / scaleFactor, MutableQuadView.BAKE_ROTATE_90),
            west = UvCoords(icornerSide1Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_ROTATE_180),
            east = UvCoords(icornerSide2Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_ROTATE_180)
          ).transform(mat).into(builder.emitter, d.texture, d.materials.standard)
        }
        Corner -> {
          finder.disableAo(0, true)
          box(
            vec3(armLength, 0f, 1f),
            vec3(1 - armLength, cableHeight, 1 + cableHeight),
            up = UvCoords(cornerTop1Uv, cableWidth / scaleFactor, cableHeight / scaleFactor),
            south = UvCoords(cornerTop2Uv, cableWidth / scaleFactor, cableHeight / scaleFactor),
            west = UvCoords(cornerSide1Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_FLIP_V),
            east = UvCoords(cornerSide2Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_FLIP_V)
          ).transform(mat).into(builder.emitter, d.texture, d.materials.corner)
          finder.clear()
        }
        Unconnected, UnconnectedCrossing -> {
          val coords = UvCoords(
            if (!swapUnconnectedSides) centerSide1Uv else centerSide2Uv,
            cableHeight / scaleFactor, cableWidth / scaleFactor,
            if (!swapUnconnectedSides) MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U else MutableQuadView.BAKE_ROTATE_270
          )
          box(
            vec3(armLength, 0f, armLength),
            vec3(1 - armLength, cableHeight, 1 - armLength),
            south = coords
          ).transform(mat).into(builder.emitter, d.texture, d.materials.standard)
        }
        else -> {
        }
      }
    } else {
      val uvTop = when (variant) {
        External, Unconnected, UnconnectedCrossing -> arm2TopUv
        Internal -> innerTop2Uv
        Corner -> arm2TopUv
        Terminal -> center8Top2Uv
      }

      val uvBottom = when (variant) {
        External, Unconnected, UnconnectedCrossing -> arm2BottomUv
        Internal -> innerBottom2Uv
        Corner -> arm2BottomUv
        Terminal -> center8Bottom2Uv
      }

      val (uvSide1, uvSide2) = when (variant) {
        External, Corner, Unconnected, UnconnectedCrossing -> Pair(arm2Side1Uv, arm2Side2Uv)
        Internal -> Pair(innerArm2Side1Uv, innerArm2Side2Uv)
        Terminal -> Pair(center8Arm2Side1Uv, center8Arm2Side2Uv)
      }

      val uvFront = cableBackUv.takeIf { variant in setOf(External, Terminal) }

      box(
        vec3(origin.x, 0f, armLength - baseLength),
        vec3(1 - armLength, cableHeight, armLength),
        down = UvCoords(uvBottom, cableWidth / scaleFactor, baseLength / scaleFactor),
        up = UvCoords(uvTop, cableWidth / scaleFactor, baseLength / scaleFactor),
        north = uvFront?.let { UvCoords(uvFront, cableHeight / scaleFactor, cableWidth / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U) },
        west = UvCoords(uvSide1, cableHeight / scaleFactor, baseLength / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U),
        east = UvCoords(uvSide2, cableHeight / scaleFactor, baseLength / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U)
      ).transform(mat).into(builder.emitter, d.texture, d.materials.standard)

      when (variant) {
        Internal -> {
          box(
            vec3(armLength, 0f, 0f),
            vec3(1 - armLength, cableHeight, cableHeight),
            up = UvCoords(cableBackUv, cableHeight / scaleFactor, cableWidth / scaleFactor, MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U),
            west = UvCoords(icornerSide1Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_FLIP_V),
            east = UvCoords(icornerSide2Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_FLIP_V)
          ).transform(mat).into(builder.emitter, d.texture, d.materials.standard)
        }
        Corner -> {
          box(
            vec3(armLength, 0f, -cableHeight),
            vec3(1 - armLength, cableHeight, 0f),
            up = UvCoords(cornerTop2Uv, cableWidth / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_FLIP_V),
            north = UvCoords(cornerTop1Uv, cableWidth / scaleFactor, cableHeight / scaleFactor),
            west = UvCoords(cornerSide1Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_ROTATE_180),
            east = UvCoords(cornerSide2Uv, cableHeight / scaleFactor, cableHeight / scaleFactor, MutableQuadView.BAKE_ROTATE_180)
          ).transform(mat).into(builder.emitter, d.texture, d.materials.corner)
        }
        Unconnected, UnconnectedCrossing -> {
          val coords = UvCoords(
            if (!swapUnconnectedSides) centerSide2Uv else centerSide1Uv,
            cableHeight / scaleFactor, cableWidth / scaleFactor,
            if (!swapUnconnectedSides) MutableQuadView.BAKE_ROTATE_90 + MutableQuadView.BAKE_FLIP_U else MutableQuadView.BAKE_ROTATE_270
          )
          box(
            vec3(armLength, 0f, armLength),
            vec3(1 - armLength, cableHeight, 1 - armLength),
            north = coords
          ).transform(mat).into(builder.emitter, d.texture, d.materials.standard)
        }
        else -> {
        }
      }
    }

    return builder.build()
  }

  override fun getTextureDependencies(function: Function<Identifier, UnbakedModel>?, set: MutableSet<com.mojang.datafixers.util.Pair<String, String>>?): Collection<SpriteIdentifier> {
    return setOf(SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, texture))
  }

  override fun getModelDependencies() = emptySet<Identifier>()

}

private data class RenderData(val materials: Materials, val texture: Sprite)

private data class Materials(val standard: RenderMaterial, val corner: RenderMaterial)

private data class UvCoords(val uv: Vector2fc, val twidth: Float, val theight: Float, val flags: Int = 0)

private data class Vertex(val x: Float, val y: Float, val z: Float, val u: Float, val v: Float) {

  constructor(xyz: Vector3fc, u: Float, v: Float) : this(xyz.x, xyz.y, xyz.z, u, v)

  fun transform(mat: Matrix4fc): Vertex {
    val vec = mat.transformProject(mutVec3(x, y, z))
    return Vertex(vec.x, vec.y, vec.z, u, v)
  }

}

private data class Quad(val v1: Vertex, val v2: Vertex, val v3: Vertex, val v4: Vertex) {

  fun sort(face: Direction): Quad {
    val all = listOf(v1, v2, v3, v4)

    val center = vec3((v1.x + v2.x + v3.x + v4.x) / 4, (v1.y + v2.y + v3.y + v4.y) / 4, (v1.z + v2.z + v3.z + v4.z) / 4)

    val (v1, v2, v3, v4) = all.sortedBy {
      when (face.axis) {
        X -> Math.atan2(-(it.z - center.z).toDouble() * face.direction.offset(), -(it.y - center.y).toDouble())
        Y -> Math.atan2(-(it.x - center.x).toDouble() * face.direction.offset(), -(it.z - center.z).toDouble())
        Z -> Math.atan2((it.x - center.x).toDouble() * face.direction.offset(), -(it.y - center.y).toDouble())
      }
    }

    return Quad(v1, v2, v3, v4)
  }

  fun into(qe: QuadEmitter, t: Sprite, mat: RenderMaterial) {
    qe.spriteColor(0, -1, -1, -1, -1)
    for ((i, q) in listOf(v1, v2, v3, v4).withIndex()) {
      qe.pos(i, q.x, q.y, q.z)
      qe.sprite(i, 0, q.u, q.v)
    }
    qe.spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED)
    qe.material(mat)
    qe.emit()
  }

  fun transform(mat: Matrix4fc) = Quad(
    v1.transform(mat),
    v2.transform(mat),
    v3.transform(mat),
    v4.transform(mat)
  )

}

private fun quad(face: Direction, xy1: Vector2fc, xy2: Vector2fc, depth: Float, uv: Vector2fc, twidth: Float, theight: Float, flags: Int): List<Quad> {
  val depth = if (face.direction == NEGATIVE) depth else 1 - depth

  fun toVec3(x: Float, y: Float): Vector3fc = when (face.axis) {
    X -> vec3(depth, y, x)
    Y -> vec3(x, depth, y)
    Z -> vec3(x, y, depth)
  }

  val (uv1, uv2, uv3, uv4) = listOf(vec2(uv.x, uv.y + theight), vec2(uv.x + twidth, uv.y + theight), vec2(uv.x + twidth, uv.y), vec2(uv.x, uv.y))
    .let { (v1, v2, v3, v4) -> if (flags and MutableQuadView.BAKE_FLIP_U != 0) listOf(v2, v1, v4, v3) else listOf(v1, v2, v3, v4) }
    .let { l -> if (flags and MutableQuadView.BAKE_FLIP_V != 0) l.reversed() else l }
    .let { (it + it).subList(flags and 3, (flags and 3) + 4) }

  return listOf(Quad(
    Vertex(toVec3(xy1.x(), xy1.y()), uv1.x, uv1.y),
    Vertex(toVec3(xy2.x(), xy1.y()), uv2.x, uv2.y),
    Vertex(toVec3(xy2.x(), xy2.y()), uv3.x, uv3.y),
    Vertex(toVec3(xy1.x(), xy2.y()), uv4.x, uv4.y)
  ).sort(face))
}

private fun box(min: Vector3fc, max: Vector3fc, down: UvCoords? = null, up: UvCoords? = null, north: UvCoords? = null, south: UvCoords? = null, west: UvCoords? = null, east: UvCoords? = null): List<Quad> {
  val quads = mutableListOf<Quad>()

  if (down != null) quads += quad(DOWN, min.xz, max.xz, min.y, down.uv, down.twidth, down.theight, down.flags)
  if (up != null) quads += quad(UP, min.xz, max.xz, 1 - max.y, up.uv, up.twidth, up.theight, up.flags)
  if (north != null) quads += quad(NORTH, min.xy, max.xy, min.z, north.uv, north.twidth, north.theight, north.flags)
  if (south != null) quads += quad(SOUTH, min.xy, max.xy, 1 - max.z, south.uv, south.twidth, south.theight, south.flags)
  if (west != null) quads += quad(WEST, min.zy, max.zy, min.x, west.uv, west.twidth, west.theight, west.flags)
  if (east != null) quads += quad(EAST, min.zy, max.zy, 1 - max.x, east.uv, east.twidth, east.theight, east.flags)

  return quads
}

private fun getExtGenInfo(side: Direction, edge: Direction): Pair<Matrix4fc, AxisDirection> {
  val rotAxis = Axis.values().single { it != side.axis && it != edge.axis }
  var rot = 0

  var start = when (rotAxis) {
    X, Z -> DOWN
    Y -> WEST
  }

  while (start != side) {
    start = start.rotateClockwise(rotAxis)
    rot += 1
  }

  val mat = Matrix4f()

  mat.translate(0.5f, 0.5f, 0.5f)
  when (rotAxis) {
    X -> mat.rotateX(-rot * 0.5f * PI.toFloat())
    Y -> mat.rotateZ(-0.5f * PI.toFloat()).rotateX(rot * 0.5f * PI.toFloat())
    Z -> mat.rotateY(0.5f * PI.toFloat()).rotateX(rot * 0.5f * PI.toFloat())
    else -> Unit
  }
  mat.translate(-0.5f, -0.5f, -0.5f)

  val dir = when (Pair(side, edge.axis)) {
    Pair(WEST, Y), Pair(EAST, Z), Pair(NORTH, X), Pair(NORTH, Y), Pair(UP, X), Pair(UP, Z) -> edge.opposite.direction
    else -> edge.direction
  }

  return Pair(mat, dir)
}

private fun Collection<Quad>.into(qe: QuadEmitter, t: Sprite, mat: RenderMaterial) = forEach { it.into(qe, t, mat) }

private fun Collection<Quad>.transform(mat: Matrix4fc) = map { it.transform(mat) }

enum class CenterVariant {
  Crossing,
  Straight1, // X axis
  Straight2, // Z axis
  Standalone,
}

enum class ExtVariant {
  External,
  Internal,
  Corner,
  Unconnected,
  UnconnectedCrossing,
  Terminal,
}