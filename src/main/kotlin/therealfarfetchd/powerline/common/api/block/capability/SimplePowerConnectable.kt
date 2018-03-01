package therealfarfetchd.powerline.common.api.block.capability

import net.minecraft.util.EnumFacing
import therealfarfetchd.powerline.common.api.PowerConductor
import therealfarfetchd.powerline.common.api.PowerType

class SimplePowerConnectable(private val conductor: PowerConductor) : IPowerConnectable {
  override fun getPowerType(facing: EnumFacing?): PowerType? = PowerType.LowVoltage

  override fun getEdge(facing: EnumFacing?): Any? = conductor
}