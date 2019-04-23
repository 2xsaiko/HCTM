package therealfarfetchd.retrocomputers.common.block

import net.minecraft.world.BlockView
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class TerminalBlock : BaseBlock() {

  override fun createBlockEntity(view: BlockView) = TerminalEntity()

}

class TerminalEntity : BaseBlockEntity(BlockEntityTypes.Terminal) {

  override var busId: Byte = 2

  override fun readData(at: Byte): Byte {
    return 0
  }

  override fun storeData(at: Byte, data: Byte) {

  }

}