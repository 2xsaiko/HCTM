package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import therealfarfetchd.retrocomputers.common.block.wire.accessIoNet
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