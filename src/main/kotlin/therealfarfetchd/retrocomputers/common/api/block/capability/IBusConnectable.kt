package therealfarfetchd.retrocomputers.common.api.block.capability

import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.api.block.BusContainer

/**
 * Created by marco on 24.06.17.
 */
interface IBusConnectable : IConnectable {
  override fun getEdge(facing: EnumFacing?): BusContainer?

  override fun getType(facing: EnumFacing?): ResourceLocation? = getEdge(facing)?.let { DataType }

  companion object {
    val DataType = ResourceLocation(ModID, "bus")
  }
}