package therealfarfetchd.retrocomputers.common.util

import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class ContextAABB<out T>(
  val context: T,
  x1: Double, y1: Double, z1: Double,
  x2: Double, y2: Double, z2: Double
) : AxisAlignedBB(x1, y1, z1, x2, y2, z2) {
  //  constructor(index: Int, pos: BlockPos) : this(index, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
  //    pos.x.toDouble() + 1, pos.y.toDouble() + 1, pos.z.toDouble() + 1)

  constructor(context: T, aabb: AxisAlignedBB) : this(context, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)

  override fun offset(pos: BlockPos) = ContextAABB(context, super.offset(pos))

  override fun offset(x: Double, y: Double, z: Double) = ContextAABB(context, super.offset(x, y, z))

  override fun offset(vec: Vec3d) = ContextAABB(context, super.offset(vec))

  override fun setMaxY(y2: Double) = ContextAABB(context, super.setMaxY(y2))

  override fun contract(x: Double, y: Double, z: Double) = ContextAABB(context, super.contract(x, y, z))

  override fun expand(x: Double, y: Double, z: Double) = ContextAABB(context, super.expand(x, y, z))

  override fun grow(value: Double) = ContextAABB(context, super.grow(value))

  override fun grow(x: Double, y: Double, z: Double) = ContextAABB(context, super.grow(x, y, z))

  override fun shrink(value: Double) = ContextAABB(context, super.shrink(value))

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as ContextAABB<*>

    if (context != other.context) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (context?.hashCode() ?: 0)
    return result
  }
}