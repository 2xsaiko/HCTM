package therealfarfetchd.retrocomputers.common.block

import net.minecraft.world.BlockView
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class DiskDriveBlock : BaseBlock() {

  override fun createBlockEntity(view: BlockView) = DiskDriveEntity()

}

class DiskDriveEntity : BaseBlockEntity(BlockEntityTypes.DiskDrive) {

}