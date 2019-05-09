package therealfarfetchd.hctm.client.render.model

import grondag.frex.api.RendererAccess
import grondag.frex.api.mesh.Mesh
import grondag.frex.api.mesh.MutableQuadView
import grondag.frex.api.mesh.QuadEmitter
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis.X
import net.minecraft.util.math.Direction.Axis.Y
import net.minecraft.util.math.Direction.Axis.Z
import net.minecraft.util.math.Direction.AxisDirection.NEGATIVE
import net.minecraft.util.math.Direction.AxisDirection.POSITIVE
import net.minecraft.util.math.Vec2f
import therealfarfetchd.hctm.client.render.model.CenterVariant.Crossing
import therealfarfetchd.hctm.client.render.model.CenterVariant.Standalone
import therealfarfetchd.hctm.client.render.model.CenterVariant.Straight1
import therealfarfetchd.hctm.client.render.model.CenterVariant.Straight2
import therealfarfetchd.hctm.client.render.model.ExtVariant.Corner
import therealfarfetchd.hctm.client.render.model.ExtVariant.External
import therealfarfetchd.hctm.client.render.model.ExtVariant.Internal
import therealfarfetchd.hctm.client.render.model.ExtVariant.Terminal
import therealfarfetchd.hctm.client.render.model.ExtVariant.Unconnected
import java.util.function.Function

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
  private val arm1TopUv = Vec2f(0.0f, 0.0f)
  private val arm2TopUv = Vec2f(0.0f, armLength + cableWidth) / scaleFactor
  private val centerTopUv = Vec2f(0.0f, armLength) / scaleFactor
  private val centerTopCUv = Vec2f(0.0f, 1.0f) / scaleFactor
  private val arm1Side1Uv = Vec2f(cableWidth, 0.0f) / scaleFactor
  private val arm2Side1Uv = Vec2f(cableWidth, armLength + cableWidth) / scaleFactor
  private val centerSide1Uv = Vec2f(cableWidth, armLength) / scaleFactor
  private val arm1Side2Uv = Vec2f(cableWidth + cableHeight, 0.0f) / scaleFactor
  private val arm2Side2Uv = Vec2f(cableWidth + cableHeight, armLength + cableWidth) / scaleFactor
  private val centerSide2Uv = Vec2f(cableWidth + cableHeight, armLength) / scaleFactor
  private val arm1BottomUv = Vec2f(cableWidth + 2 * cableHeight, 0.0f) / scaleFactor
  private val arm2BottomUv = Vec2f(cableWidth + 2 * cableHeight, armLength + cableWidth) / scaleFactor
  private val centerBottomUv = Vec2f(cableWidth + 2 * cableHeight, armLength) / scaleFactor
  private val cableFrontUv = Vec2f(cableWidth, 1.0f) / scaleFactor
  private val cableBackUv = Vec2f(cableWidth + cableHeight, 1.0f) / scaleFactor
  private val cornerTop1Uv = Vec2f(0.0f, 1.0f + cableWidth) / scaleFactor
  private val cornerTop2Uv = Vec2f(cableWidth + 2 * cableHeight, 1.0f + cableWidth) / scaleFactor
  private val cornerSide1Uv = Vec2f(cableWidth, 1.0f + cableWidth) / scaleFactor
  private val cornerSide2Uv = Vec2f(cableWidth + cableHeight, 1.0f + cableWidth) / scaleFactor
  private val icornerSide1Uv = Vec2f(2 * cableWidth + 2 * cableHeight, 0.0f) / scaleFactor
  private val icornerSide2Uv = Vec2f(2 * cableWidth + 2 * cableHeight, cableHeight) / scaleFactor
  private val center8Top1Uv = Vec2f(0.0f, 0.25f) / scaleFactor
  private val center8Top2Uv = arm2TopUv
  private val center8Bottom1Uv = Vec2f(cableWidth + 2 * cableHeight, 0.25f) / scaleFactor
  private val center8Bottom2Uv = arm2BottomUv
  private val center8Arm1Side1Uv = Vec2f(cableWidth, 0.25f) / scaleFactor
  private val center8Arm1Side2Uv = Vec2f(cableWidth + cableHeight, 0.25f) / scaleFactor
  private val center8Arm2Side1Uv = arm2Side1Uv
  private val center8Arm2Side2Uv = arm2Side2Uv
  private val innerTop1Uv = Vec2f(0.0f, armInnerSp) / scaleFactor
  private val innerTop2Uv = arm2TopUv
  private val innerBottom1Uv = Vec2f(cableWidth + 2 * cableHeight, armInnerSp) / scaleFactor
  private val innerBottom2Uv = arm2BottomUv
  private val innerArm1Side1Uv = Vec2f(cableWidth, armInnerSp) / scaleFactor
  private val innerArm1Side2Uv = Vec2f(cableWidth + cableHeight, armInnerSp) / scaleFactor
  private val innerArm2Side1Uv = arm2Side1Uv
  private val innerArm2Side2Uv = arm2Side2Uv

  val renderer = RendererAccess.INSTANCE.renderer
  val builder = renderer.meshBuilder()
  val finder = renderer.materialFinder()

  override fun getModelDependencies() = emptySet<Identifier>()

  override fun bake(ml: ModelLoader, getTexture: Function<Identifier, Sprite>, settings: ModelBakeSettings): BakedModel {
    val parts = generateParts(getTexture.apply(texture))

    return WireModel(getTexture.apply(texture), parts)
  }

  private fun generateParts(t: Sprite): WireModelParts {
    return WireModelParts(Direction.values().toList().associateWith { generateSide(t, it) })
  }

  private fun generateSide(t: Sprite, side: Direction): WireModelPart {
    val center = generateCenter(t, side, Straight2)

    val exts = mutableMapOf<Pair<Direction, ConnectionType>, Mesh>()

    for (ext in Direction.values().filter { it.axis != side.axis }) {
      val external = generateExt(t, side, ext, External)

      exts[Pair(ext, ConnectionType.EXTERNAL)] = external
      exts[Pair(ext, ConnectionType.INTERNAL)] = external

      exts[Pair(ext, ConnectionType.CORNER)] = builder.build()
    }

    return WireModelPart(center, exts)
  }

  private fun generateCenter(t: Sprite, side: Direction, variant: CenterVariant): Mesh {
    val origin = Vec2f(armLength, armLength)
    val size = Vec2f(cableWidth, cableWidth)

    val uvTop = when (variant) {
      Crossing -> centerTopCUv
      Straight1, Straight2, Standalone -> centerTopUv
    }

    val uvBottom = when (variant) {
      Crossing -> centerTopCUv
      Straight1, Straight2, Standalone -> centerBottomUv
    }

    val flags = when (variant) {
      Crossing, Straight1, Standalone -> combine(MutableQuadView.BAKE_ROTATE_90, if (side == Direction.UP) MutableQuadView.BAKE_FLIP_U else 0)
      Straight2 -> if (side in setOf(Direction.SOUTH, Direction.WEST)) MutableQuadView.BAKE_FLIP_U else 0
    }

    builder.emitter.prepare()
      .square(side.opposite, origin.x, origin.y, origin.x + size.x, origin.y + size.y, 1 - cableHeight)
      .uv(0, uvTop, size.x, size.y, MutableQuadView.BAKE_NORMALIZED or flags)
      .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
      .emit()
    builder.emitter.prepare()
      .square(side, origin.x, origin.y, origin.x + size.x, origin.y + size.y, 0f)
      .uv(0, uvBottom, size.x, size.y, MutableQuadView.BAKE_NORMALIZED or flags)
      .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
      .emit()

    return builder.build()
  }

  private fun generateExt(t: Sprite, side: Direction, edge: Direction, variant: ExtVariant): Mesh {
    val baseLength = when (variant) {
      External -> armLength
      Internal -> armInnerLength
      Corner -> armLength
      Unconnected -> 0f
      Terminal -> armLength - 0.25f
    }

    val origin = Vec2f(armLength, armLength - baseLength)

    val useTop =
      (edge.direction == NEGATIVE) xor
        (side != Direction.DOWN) xor
        (side == Direction.DOWN && edge.axis == X) xor
        (side == Direction.WEST && edge.axis == Z) xor
        (side == Direction.SOUTH && edge.axis == X)

    val uvTop = when (Pair(variant, useTop)) {
      Pair(External, true) -> arm1TopUv
      Pair(External, false) -> arm2TopUv
      Pair(Internal, true) -> innerTop1Uv
      Pair(Internal, false) -> innerTop2Uv
      Pair(Corner, true) -> arm1TopUv
      Pair(Corner, false) -> arm2TopUv
      Pair(Unconnected, true) -> centerTopUv // unused
      Pair(Unconnected, false) -> arm2TopUv // unused
      Pair(Terminal, true) -> center8Top1Uv
      Pair(Terminal, false) -> center8Top2Uv
      else -> error("unreachable")
    }

    val uvBottom = when (variant) {
      External -> arm1BottomUv
      Internal -> innerBottom1Uv
      Corner -> arm1BottomUv
      Unconnected -> centerTopUv // unused
      Terminal -> center8Bottom1Uv
    }

    val needsSides = baseLength > 0f

    // this is related to front quad, not the special front (for corner/internal)
    val needsFront = when (variant) {
      External, Unconnected, Terminal -> true
      Internal, Corner -> false
    }

    if (needsSides) {
      val quadRotMap = mapOf(
        Direction.DOWN to mapOf(
          Direction.NORTH to 2,
          Direction.SOUTH to 0,
          Direction.WEST to 3,
          Direction.EAST to 1
        ),
        Direction.UP to mapOf(
          Direction.NORTH to 0,
          Direction.SOUTH to 2,
          Direction.WEST to 3,
          Direction.EAST to 1
        ),
        Direction.NORTH to mapOf(
          Direction.DOWN to 0,
          Direction.UP to 2,
          Direction.WEST to 1,
          Direction.EAST to 3
        ),
        Direction.SOUTH to mapOf(
          Direction.DOWN to 0,
          Direction.UP to 2,
          Direction.WEST to 3,
          Direction.EAST to 1
        ),
        Direction.WEST to mapOf(
          Direction.DOWN to 0,
          Direction.UP to 2,
          Direction.NORTH to 1,
          Direction.SOUTH to 3
        ),
        Direction.EAST to mapOf(
          Direction.DOWN to 0,
          Direction.UP to 2,
          Direction.NORTH to 3,
          Direction.SOUTH to 1
        )
      )

      val quadRot = quadRotMap.getValue(side).getValue(edge)

      var flags = 0
      if (side in setOf(Direction.SOUTH, Direction.WEST) && edge.axis == Y) flags = combine(flags, MutableQuadView.BAKE_FLIP_U)
      if (side == Direction.UP && edge.axis == X) flags = combine(flags, MutableQuadView.BAKE_FLIP_U)
      if (quadRot and 1 != 0) flags = combine(flags, MutableQuadView.BAKE_ROTATE_270)

      builder.emitter.prepare()
        .squareRotY(side.opposite, origin.x, origin.y, origin.x + cableWidth, origin.y + baseLength, 1 - cableHeight, quadRot)
        .uv(0, uvTop, cableWidth, baseLength, MutableQuadView.BAKE_NORMALIZED or flags)
        .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
        .emit()
      builder.emitter.prepare()
        .squareRotY(side, origin.x, origin.y, origin.x + cableWidth, origin.y + baseLength, 0f, quadRot)
        .uv(0, uvBottom, cableWidth, baseLength, MutableQuadView.BAKE_NORMALIZED or flags)
        .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
        .emit()

      appendSide(t, side, edge)
    }

    if (needsFront) {
      appendFront(t, side, edge, origin.y, useTop)
    }

    return builder.build()
  }

  private fun appendSide(t: Sprite, side: Direction, edge: Direction) {
    val (a, b) = Direction.values().filter { it.axis != side.axis && it.axis != edge.axis }

    val flags = 0

    val origin = when (edge.direction) {
      POSITIVE -> Vec2f(1f - armLength, 0f)
      NEGATIVE -> Vec2f(0f, 0f)
    }

    val key = 0
    val list = listOf(arm1Side1Uv, arm1Side2Uv, arm2Side1Uv, arm2Side2Uv)
    val side1 = list[key]
    val side2 = list[key xor 1]

    val quadRotMap = mapOf(
      Direction.DOWN to mapOf(
        Direction.NORTH to 0,
        Direction.SOUTH to 0,
        Direction.WEST to 0,
        Direction.EAST to 0
      ),
      Direction.UP to mapOf(
        Direction.NORTH to 2,
        Direction.SOUTH to 2,
        Direction.WEST to 2,
        Direction.EAST to 2
      ),
      Direction.NORTH to mapOf(
        Direction.DOWN to 0,
        Direction.UP to 2,
        Direction.WEST to 3,
        Direction.EAST to 1
      ),
      Direction.SOUTH to mapOf(
        Direction.DOWN to 2,
        Direction.UP to 0,
        Direction.WEST to 1,
        Direction.EAST to 3
      ),
      Direction.WEST to mapOf(
        Direction.DOWN to 3,
        Direction.UP to 3,
        Direction.NORTH to 3,
        Direction.SOUTH to 1
      ),
      Direction.EAST to mapOf(
        Direction.DOWN to 1,
        Direction.UP to 1,
        Direction.NORTH to 1,
        Direction.SOUTH to 3
      )
    )

    if (edge != Direction.WEST || side != Direction.SOUTH) return

    val quadRot = 0 //quadRotMap.getValue(side).getValue(edge)

    builder.emitter.prepare()
      .squareRotY(a, origin.x, origin.y, origin.x + armLength, origin.y + cableHeight, armLength, quadRot)
      .uv(0, side1, cableHeight, armLength, MutableQuadView.BAKE_NORMALIZED or flags)
      .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
      .emit()
    builder.emitter.prepare()
      .squareRotY(b, origin.x, origin.y, origin.x + armLength, origin.y + cableHeight, armLength, quadRot)
      .uv(0, side2, cableHeight, armLength, MutableQuadView.BAKE_NORMALIZED or flags)
      .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
      .emit()
  }

  private fun appendFront(t: Sprite, side: Direction, edge: Direction, depth: Float, useTop: Boolean) {
    val uvFront = when (useTop) {
      true -> cableFrontUv
      false -> cableBackUv
    }

    val quadRotMap = mapOf(
      Direction.DOWN to mapOf(
        Direction.NORTH to 0,
        Direction.SOUTH to 0,
        Direction.WEST to 0,
        Direction.EAST to 0
      ),
      Direction.UP to mapOf(
        Direction.NORTH to 2,
        Direction.SOUTH to 2,
        Direction.WEST to 2,
        Direction.EAST to 2
      ),
      Direction.NORTH to mapOf(
        Direction.DOWN to 0,
        Direction.UP to 2,
        Direction.WEST to 3,
        Direction.EAST to 1
      ),
      Direction.SOUTH to mapOf(
        Direction.DOWN to 2,
        Direction.UP to 0,
        Direction.WEST to 1,
        Direction.EAST to 3
      ),
      Direction.WEST to mapOf(
        Direction.DOWN to 3,
        Direction.UP to 3,
        Direction.NORTH to 3,
        Direction.SOUTH to 1
      ),
      Direction.EAST to mapOf(
        Direction.DOWN to 1,
        Direction.UP to 1,
        Direction.NORTH to 1,
        Direction.SOUTH to 3
      )
    )

    val quadRot = quadRotMap.getValue(side).getValue(edge)

    var flags = 0
    if (flags and 1 == 0) flags = combine(flags, MutableQuadView.BAKE_ROTATE_90)
    if (side.axis == X) flags = combine(flags, MutableQuadView.BAKE_ROTATE_90)
    if (side.axis == Z && edge.axis == X) flags = combine(flags, MutableQuadView.BAKE_ROTATE_90)
    if (side.axis == Y && edge in setOf(Direction.EAST, Direction.NORTH)) flags = combine(flags, MutableQuadView.BAKE_FLIP_V)
    if (side.axis != Y) flags = combine(flags, MutableQuadView.BAKE_FLIP_V)
    if (edge == Direction.DOWN) flags = combine(flags, MutableQuadView.BAKE_FLIP_V)
    if (edge == Direction.UP && side.axis == Z) flags = combine(flags, MutableQuadView.BAKE_FLIP_V)

    builder.emitter.prepare()
      .squareRotY(edge, armLength, 0f, 1 - armLength, cableHeight, depth, quadRot)
      .uv(0, uvFront, cableHeight, cableWidth, MutableQuadView.BAKE_NORMALIZED or flags)
      .spriteBake(0, t, MutableQuadView.BAKE_NORMALIZED or flags)
      .emit()
  }

  private fun rotate(uv1: Vec2f, uv2: Vec2f, flags: Int): Pair<Vec2f, Vec2f> {
    var uv1r = uv1
    var uv2r = uv2
    var uv1 = uv1r
    var uv2 = uv2r

    val max = if (flags and MutableQuadView.BAKE_NORMALIZED != 0) 1f else 16f

    if (flags and MutableQuadView.BAKE_FLIP_U != 0) {
      uv1r = Vec2f(max - uv2.x, uv1.y)
      uv2r = Vec2f(max - uv1.x, uv2.y)
    }

    uv1 = uv1r
    uv2 = uv2r

    if (flags and MutableQuadView.BAKE_FLIP_V != 0) {
      uv1r = Vec2f(uv1.x, max - uv2.y)
      uv2r = Vec2f(uv2.x, max - uv1.y)
    }

    uv1 = uv1r
    uv2 = uv2r

    when (flags and 3) {
      MutableQuadView.BAKE_ROTATE_90 -> {
        uv1r = Vec2f(max - uv2.y, uv1.x)
        uv2r = Vec2f(max - uv1.y, uv2.x)
      }
      MutableQuadView.BAKE_ROTATE_180 -> {
        uv1r = Vec2f(max - uv2.x, max - uv2.y)
        uv2r = Vec2f(max - uv1.x, max - uv1.y)
      }
      MutableQuadView.BAKE_ROTATE_270 -> {
        uv1r = Vec2f(uv1.y, max - uv2.x)
        uv2r = Vec2f(uv2.y, max - uv1.x)
      }
    }

    return Pair(uv1r, uv2r)
  }

  private fun combine(flags: Int, flags2: Int): Int =
    (flags + flags2 and 0b000011) or
      (flags xor flags2 and 0b011100) or
      (flags or flags2 and 0b100000)

  private fun QuadEmitter.prepare() = spriteColor(0, -1, -1, -1, -1)

  private fun QuadEmitter.uv(spriteIndex: Int, uv1: Vec2f, twidth: Float, theight: Float, flags: Int): QuadEmitter {
    val (uv1, uv2) = rotate(uv1, Vec2f(uv1.x + twidth / scaleFactor, uv1.y + theight / scaleFactor), flags)

    return this
      .sprite(0, spriteIndex, uv1.x, uv1.y)
      .sprite(1, spriteIndex, uv1.x, uv2.y)
      .sprite(2, spriteIndex, uv2.x, uv2.y)
      .sprite(3, spriteIndex, uv2.x, uv1.y)
  }

  private fun QuadEmitter.squareRotY(nominalFace: Direction, left: Float, bottom: Float, right: Float, top: Float, depth: Float, rotate: Int): QuadEmitter {
    val (xy1, xy2) = rotate(Vec2f(left, top), Vec2f(right, bottom), rotate and 3 or MutableQuadView.BAKE_NORMALIZED)
    return this.square(nominalFace, xy1.x, xy2.y, xy2.x, xy1.y, depth)
  }

  private fun QuadEmitter.squareSpecial(nominalFace: Direction, rYFirst: Int, left: Float, bottom: Float, right: Float, top: Float, depth: Float): QuadEmitter {
    val face = nominalFace.rotate(rYFirst, Y)
    return this.square(face, left, bottom, right, top, depth)
  }

  private tailrec fun Direction.rotate(n: Int, axis: Direction.Axis): Direction {
    val n = n and 3
    if (n == 0) return this
    return rotateClockwise(axis).rotate(n - 1, axis)
  }

  override fun getTextureDependencies(getModel: Function<Identifier, UnbakedModel>, errors: MutableSet<String>): Collection<Identifier> {
    return setOf(texture)
  }

}

private operator fun Vec2f.plus(v: Vec2f) = Vec2f(x + v.x, y + v.y)

private operator fun Vec2f.minus(v: Vec2f) = Vec2f(x - v.x, y - v.y)

private operator fun Vec2f.times(v: Vec2f) = Vec2f(x * v.x, y * v.y)

private operator fun Vec2f.times(f: Float) = Vec2f(x * f, y * f)

private operator fun Vec2f.div(f: Float) = Vec2f(x / f, y / f)

private enum class CenterVariant {
  Crossing,
  Straight1, // X axis
  Straight2, // Z axis
  Standalone,
}

private enum class ExtVariant {
  External,
  Internal,
  Corner,
  Unconnected,
  Terminal,
}