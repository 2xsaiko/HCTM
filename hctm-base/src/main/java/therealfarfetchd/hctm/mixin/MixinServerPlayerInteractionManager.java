package therealfarfetchd.hctm.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import therealfarfetchd.hctm.common.block.ext.BlockCustomBreak;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class MixinServerPlayerInteractionManager {

    @Shadow public ServerWorld world;

    @Shadow public ServerPlayerEntity player;

    @Inject(
        method = "tryBreakBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V",
            shift = Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void tryBreakBlock(BlockPos blockPos_1, CallbackInfoReturnable<Boolean> cir, BlockState blockState_1) {
        Block block = blockState_1.getBlock();
        if (block instanceof BlockCustomBreak) {
            if (!((BlockCustomBreak) block).tryBreak(blockState_1, blockPos_1, world, player)) {
                cir.setReturnValue(false);
            }
        }
    }

}
