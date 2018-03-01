package therealfarfetchd.retrocomputers.common.block.capability

import net.minecraft.util.EnumFacing
import therealfarfetchd.rswires.common.api.block.IRedstoneConductor
import therealfarfetchd.rswires.common.api.block.capability.IRedstoneConnectable

class RedstonePortConnectable(val top: () -> Boolean, val rs: IRedstoneConductor) : IRedstoneConnectable {
  override fun getEdge(facing: EnumFacing?): IRedstoneConductor? {
    return if ((top() && facing == EnumFacing.UP) || (!top() && facing == EnumFacing.DOWN)) rs
    else null
  }
}