package therealfarfetchd.powerline.common.block.capability

import net.minecraft.util.EnumFacing
import therealfarfetchd.powerline.common.api.PowerType
import therealfarfetchd.powerline.common.api.block.capability.IPowerConnectable

class TransformerPowerConnectable(val transformer: Any,val hvSide: Boolean) : IPowerConnectable {
  override fun getPowerType(facing: EnumFacing?): PowerType? {
    return if (facing == EnumFacing.DOWN) if (hvSide) PowerType.HighVoltage else PowerType.LowVoltage else null
  }

  override fun getEdge(facing: EnumFacing?): Any? {
    return if (facing == EnumFacing.DOWN) transformer else null
  }
}