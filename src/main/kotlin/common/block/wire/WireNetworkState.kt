package therealfarfetchd.retrocomputers.common.block.wire

import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.dimension.Dimension

class WireNetworkState(private val world: ServerWorld) : PersistentState(getNameForDimension(world.getDimension())) {

  fun onBlockChanged(pos: BlockPos, state: BlockState) {

  }

  override fun toTag(tag: CompoundTag): CompoundTag {
    return tag
  }

  override fun fromTag(tag: CompoundTag) {

  }

  companion object {
    fun getNameForDimension(dimension: Dimension) = "wirenet${dimension.type.suffix}"
  }

}

class Node<T : PartExt>(val part: NetworkPart<T>, var connections: List<Node<*>>) {
  fun toTag(tag: CompoundTag): CompoundTag {
    return tag
  }

  fun fromTag(tag: CompoundTag) {

  }
}

class NetworkPart<T : PartExt>(var pos: BlockPos, val ext: T) {
  fun toTag(tag: CompoundTag): CompoundTag {
    tag.putInt("x", pos.x)
    tag.putInt("y", pos.y)
    tag.putInt("z", pos.z)
    tag.put("ext", ext.toTag(CompoundTag()))
    return tag
  }

  fun fromTag(tag: CompoundTag) {

  }
}

interface PartExt {
  fun toTag(tag: CompoundTag): CompoundTag

  fun fromTag(tag: CompoundTag)
}

fun ServerWorld.getWireNetworkState(): WireNetworkState {
  val dimension = getDimension()
  return persistentStateManager.getOrCreate({ WireNetworkState(this) }, WireNetworkState.getNameForDimension(dimension))
}