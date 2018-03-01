package therealfarfetchd.powerline.common.block.capability

import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.common.api.block.capability.IPowerConnectable
import therealfarfetchd.powerline.common.block.FluidPipe
import therealfarfetchd.powerline.common.block.Pump
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable

class PumpConnectable(val facing: EnumFacing, val pump: Pump) : IConnectable {

  private val adjustedBase: EnumFacing
    get() = Pump.rot(pump.facing, facing)!!

  override fun getEdge(facing: EnumFacing?): Any? {
    return when (getType(facing)) {
      IPowerConnectable.TypeLV -> pump.cond
      FluidPipe.DataType -> if (adjustedBase == NORTH) pump.fluidIn else pump.fluidOut
      else -> null
    }
  }

  override fun getType(facing: EnumFacing?): ResourceLocation? {
    val adj = Pump.rot(pump.facing, facing)
    return when (adjustedBase) {
      DOWN -> IPowerConnectable.TypeLV
      UP -> if (adj == NORTH) IPowerConnectable.TypeLV else null
      NORTH, SOUTH -> if (adj == null) FluidPipe.DataType else IPowerConnectable.TypeLV
      WEST, EAST -> if (adj in setOf(NORTH, DOWN)) IPowerConnectable.TypeLV else null
    }
  }

  override fun getAdditionalData(facing: EnumFacing?, key: String): Any? {
    if (getType(facing) == FluidPipe.DataType && key == "joints") return true
    return super.getAdditionalData(facing, key)
  }
}