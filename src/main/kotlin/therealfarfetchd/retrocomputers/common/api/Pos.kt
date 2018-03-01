package therealfarfetchd.retrocomputers.common.api

import net.minecraft.util.math.BlockPos

/**
 * Created by marco on 27.05.17.
 */

operator fun BlockPos.plus(other: BlockPos): BlockPos = this.add(other)!!
operator fun BlockPos.minus(other: BlockPos): BlockPos = this.subtract(other)!!