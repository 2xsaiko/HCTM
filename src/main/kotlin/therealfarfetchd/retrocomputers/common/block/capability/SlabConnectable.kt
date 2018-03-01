package therealfarfetchd.retrocomputers.common.block.capability

import net.minecraft.util.EnumFacing
import therealfarfetchd.retrocomputers.common.api.block.BusContainer
import therealfarfetchd.retrocomputers.common.api.block.capability.IBusConnectable

class SlabConnectable(val c: BusContainer, val top: () -> Boolean) : IBusConnectable {
  override fun getEdge(facing: EnumFacing?): BusContainer? {
    return if ((top() && facing == EnumFacing.UP) || (!top() && facing == EnumFacing.DOWN)) c
    else null
  }
}