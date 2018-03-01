package therealfarfetchd.powerline.common.api.block.capability

import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.common.api.block.FluidPipeContainer
import therealfarfetchd.powerline.common.block.FluidPipe
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable

class SimpleFluidConnectable(val c: FluidPipeContainer) : IConnectable {
  override fun getEdge(facing: EnumFacing?): FluidPipeContainer? {
    return if (facing == null) c
    else null
  }

  override fun getType(facing: EnumFacing?): ResourceLocation? {
    return if (facing == null) FluidPipe.DataType
    else null
  }

  override fun getAdditionalData(facing: EnumFacing?, key: String): Any? {
    return if (facing == null && key == "joints") true
    else null
  }
}