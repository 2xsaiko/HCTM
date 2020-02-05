package net.dblsaiko.hctm.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dblsaiko.hctm.common.block.ext.BlockCustomBreak;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {

    @Shadow @Final private MinecraftClient client;

    @Inject(
        method = "breakBlock(Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V",
            shift = Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, World world, BlockState blockState, Block block) {
        if (block instanceof BlockCustomBreak) {
            if (!((BlockCustomBreak) block).tryBreak(blockState, pos, world, client.player, world.getBlockEntity(pos))) {
                cir.setReturnValue(false);
            }
        }
    }

}
