package therealfarfetchd.retrocomputers.common.api.component

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import therealfarfetchd.retrocomputers.common.api.block.BusContainer

interface ComponentContainer {
  val world: World

  val pos: BlockPos

  val facing: EnumFacing

  val slotCount: Int

  fun setComponent(slot: Int, component: Component?)

  fun getComponent(slot: Int): Component

  fun extractComponent(slot: Int)

  fun getComponents(): Iterable<Component>

  fun getSlotId(component: Component): Int?

  fun resolveNetwork(): Set<BusContainer>

  fun markDirty()
}