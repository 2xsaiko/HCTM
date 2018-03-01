package therealfarfetchd.powerline.common.block.capability

import net.minecraft.util.EnumFacing
import therealfarfetchd.powerline.common.api.PowerConductor
import therealfarfetchd.powerline.common.api.PowerType
import therealfarfetchd.powerline.common.api.block.capability.IPowerConnectable

class SolarPanelConnectable(val cond: PowerConductor) : IPowerConnectable {
  override fun getPowerType(facing: EnumFacing?): PowerType? = if (facing == EnumFacing.DOWN) PowerType.LowVoltage else null

  override fun getEdge(facing: EnumFacing?): Any? = cond.takeIf { getPowerType(facing) != null }
}