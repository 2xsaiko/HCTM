package therealfarfetchd.retrocomputers.common.block

import net.minecraft.world.BlockView
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class ComputerBlock : BaseBlock() {

  override fun createBlockEntity(view: BlockView) = ComputerEntity()

}

class ComputerEntity : BaseBlockEntity(BlockEntityTypes.Computer) {

}