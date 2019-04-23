package therealfarfetchd.retrocomputers.common.block

import net.minecraft.world.BlockView
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class DiskDriveBlock : BaseBlock() {

  override fun createBlockEntity(view: BlockView) = DiskDriveEntity()

}

class DiskDriveEntity : BaseBlockEntity(BlockEntityTypes.DiskDrive) {

  override var busId: Byte = 1

  override fun readData(at: Byte): Byte {
    return 0
  }

  override fun storeData(at: Byte, data: Byte) {

  }

}