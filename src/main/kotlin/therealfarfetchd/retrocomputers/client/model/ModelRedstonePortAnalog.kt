package therealfarfetchd.retrocomputers.client.model

import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.retrocomputers.common.block.RedstonePortAnalog

object ModelRedstonePortAnalog : ModelRedstonePortBase() {
  override val texturePrefix: String = "rs_analog"

  override fun isTop(state: IExtendedBlockState): Boolean = false

  override fun getDisplay(state: IExtendedBlockState): Short {
    val st = state[RedstonePortAnalog.PropOutput]
    return (Math.pow(2.0, st.toDouble()).toInt() - 1).toShort()
  }
}