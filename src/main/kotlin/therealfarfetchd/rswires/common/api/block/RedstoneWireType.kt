package therealfarfetchd.rswires.common.api.block

import net.minecraft.item.EnumDyeColor
import therealfarfetchd.rswires.common.util.EnumBundledWireColor

interface RedstoneWireType

object TypeRedAlloy : RedstoneWireType

data class TypeInsulated(val color: EnumDyeColor) : RedstoneWireType

data class TypeBundled(val color: EnumBundledWireColor) : RedstoneWireType