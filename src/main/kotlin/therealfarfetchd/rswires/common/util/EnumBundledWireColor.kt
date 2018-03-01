package therealfarfetchd.rswires.common.util

import net.minecraft.item.EnumDyeColor
import net.minecraft.util.IStringSerializable

enum class EnumBundledWireColor(val dyeColor: EnumDyeColor?) : IStringSerializable {
  None(null),
  White(EnumDyeColor.WHITE),
  Orange(EnumDyeColor.ORANGE),
  Magenta(EnumDyeColor.MAGENTA),
  LightBlue(EnumDyeColor.LIGHT_BLUE),
  Yellow(EnumDyeColor.YELLOW),
  Lime(EnumDyeColor.LIME),
  Pink(EnumDyeColor.PINK),
  Gray(EnumDyeColor.GRAY),
  Silver(EnumDyeColor.SILVER),
  Cyan(EnumDyeColor.CYAN),
  Purple(EnumDyeColor.PURPLE),
  Blue(EnumDyeColor.BLUE),
  Brown(EnumDyeColor.BROWN),
  Green(EnumDyeColor.GREEN),
  Red(EnumDyeColor.RED),
  Black(EnumDyeColor.BLACK);

  override fun getName(): String = dyeColor?.getName() ?: "none"

  companion object {
    fun byMetadata(meta: Int): EnumBundledWireColor {
      if (meta in 0 until values().size) return values()[meta]
      return None
    }
  }
}