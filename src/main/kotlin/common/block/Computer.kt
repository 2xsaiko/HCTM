package therealfarfetchd.retrocomputers.common.block

import net.minecraft.world.BlockView
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class ComputerBlock : BaseBlock() {

  override fun createBlockEntity(view: BlockView) = ComputerEntity()

}

class ComputerEntity : BaseBlockEntity(BlockEntityTypes.Computer) {

  override var busId: Byte = 0

  override fun readData(at: Byte): Byte {
    return 0
  }

  override fun storeData(at: Byte, data: Byte) {

  }

}