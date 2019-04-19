package therealfarfetchd.retrocomputers.common.block

import net.minecraft.world.BlockView
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class TerminalBlock : BaseBlock() {

  override fun createBlockEntity(view: BlockView) = TerminalEntity()

}

class TerminalEntity : BaseBlockEntity(BlockEntityTypes.Terminal) {

}