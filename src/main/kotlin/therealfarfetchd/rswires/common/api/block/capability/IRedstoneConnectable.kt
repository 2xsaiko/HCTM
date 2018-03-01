package therealfarfetchd.rswires.common.api.block.capability

import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.api.block.IRedstoneConductor

interface IRedstoneConnectable : IConnectable {
  override fun getEdge(facing: EnumFacing?): IRedstoneConductor?

  override fun getType(facing: EnumFacing?): ResourceLocation? = getEdge(facing)?.let { DataType }

  companion object {
    val DataType = ResourceLocation(ModID, "redstone")
  }
}