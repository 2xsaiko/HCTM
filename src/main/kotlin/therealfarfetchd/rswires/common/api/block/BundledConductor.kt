package therealfarfetchd.rswires.common.api.block

import net.minecraft.item.EnumDyeColor
import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.rswires.common.util.EnumBundledWireColor

class BundledConductor(
  ns: INeighborSupport<IRedstoneConductor>,
  rsIn: (EnumDyeColor) -> Boolean,
  rsOut: (EnumDyeColor, Boolean) -> Unit,
  endCallback: (EnumDyeColor) -> Unit = {}
) : RedstoneConductor<EnumDyeColor>(
  ns,
  rsIn,
  rsOut,
  endCallback,
  { TypeBundled(EnumBundledWireColor.None) },
  { otherType, otherChannel ->
    when (otherType) {
      is TypeBundled -> otherChannel as EnumDyeColor
      is TypeInsulated -> otherType.color
      else -> error("Wrong turn, buddy.")
    }
  },
  { _, _ -> true }
)