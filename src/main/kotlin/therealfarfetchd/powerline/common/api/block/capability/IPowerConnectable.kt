package therealfarfetchd.powerline.common.api.block.capability

import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.PowerType
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable

interface IPowerConnectable : IConnectable {

  override fun getType(facing: EnumFacing?): ResourceLocation? = getConnectionType(getPowerType(facing))

  fun getPowerType(facing: EnumFacing?): PowerType?

  companion object {
    val TypeLV = ResourceLocation(ModID, "low_voltage")
    val TypeHV = ResourceLocation(ModID, "high_voltage")

    fun getConnectionType(pt: PowerType?): ResourceLocation? =
      when (pt) {
        PowerType.LowVoltage -> TypeLV
        PowerType.HighVoltage -> TypeHV
        else -> null
      }
  }
}