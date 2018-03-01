package therealfarfetchd.retrocomputers.client.model

import net.minecraftforge.common.property.IExtendedBlockState
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.retrocomputers.common.block.RedstonePort

object ModelRedstonePort : ModelRedstonePortBase() {
  override val texturePrefix: String = "rs"

  override fun isTop(state: IExtendedBlockState): Boolean = state[RedstonePort.PropTop]

  override fun getDisplay(state: IExtendedBlockState): Short {
    return state[RedstonePort.PropOutput].toShort()
  }
}