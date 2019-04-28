package therealfarfetchd.hctm.mixin;

import net.minecraft.client.render.GameRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.hctm.client.render.DebugNodeRendererKt;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(method = "renderCenter", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableFog()V"))
    private void drawWorldLast(float delta, long time, CallbackInfo ci) {
        DebugNodeRendererKt.draw(delta);
        GlStateManager.enableTexture();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.color4f(1f, 1f, 1f, 1f);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
    }

}
