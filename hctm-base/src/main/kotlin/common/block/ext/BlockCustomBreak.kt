package therealfarfetchd.hctm.common.block.ext

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface BlockCustomBreak {

  fun tryBreak(state: BlockState, pos: BlockPos, world: World, player: PlayerEntity, blockEntity: BlockEntity?): Boolean

}