package therealfarfetchd.hctm.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import com.mojang.blaze3d.platform.GlStateManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import therealfarfetchd.hctm.common.block.ext.BlockAdvancedShape;

import static net.minecraft.client.render.WorldRenderer.drawShapeOutline;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow @Final private MinecraftClient client;

    @Shadow private ClientWorld world;

    @Inject(
        method = "drawHighlightedBlockOutline(Lnet/minecraft/client/render/Camera;Lnet/minecraft/util/hit/HitResult;I)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;enableBlend()V"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void drawHighlightedBlockOutline(Camera camera_1, HitResult hitResult_1, int int_1, CallbackInfo ci, BlockPos blockPos_1, BlockState blockState_1) {
        if (!(blockState_1.getBlock() instanceof BlockAdvancedShape)) return;
        BlockAdvancedShape bas = (BlockAdvancedShape) blockState_1.getBlock();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.lineWidth(Math.max(2.5F, (float) this.client.window.getFramebufferWidth() / 1920.0F * 2.5F));
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.0F, 1.0F, 0.999F);
        double double_1 = camera_1.getPos().x;
        double double_2 = camera_1.getPos().y;
        double double_3 = camera_1.getPos().z;
        drawShapeOutline(blockState_1.getOutlineShape(this.world, blockPos_1, EntityContext.of(camera_1.getFocusedEntity())), (double) blockPos_1.getX() - double_1, (double) blockPos_1.getY() - double_2, (double) blockPos_1.getZ() - double_3, 0.0F, 0.0F, 0.0F, 0.4F);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        ci.cancel();
    }

}
