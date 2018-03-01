package therealfarfetchd.retrocomputers.common.api.block.capability

import net.minecraft.util.EnumFacing
import therealfarfetchd.retrocomputers.common.api.block.BusContainer

class SimpleBusConnectable(val c: BusContainer) : IBusConnectable {
  override fun getEdge(facing: EnumFacing?): BusContainer? {
    return c.takeUnless { facing == null }
  }
}