package net.dblsaiko.hctm.mixin;

import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

//    @Inject(method = "renderWorld", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableFog()V"))
//    private void drawWorldLast(float delta, long time, MatrixStack stack, CallbackInfo ci) {
//        DebugNodeRendererKt.draw(delta);
//        RenderSystem.enableTexture();
//        RenderSystem.enableDepthTest();
//        RenderSystem.depthMask(true);
//        RenderSystem.enableCull();
//        RenderSystem.color4f(1f, 1f, 1f, 1f);
//        RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
//        RenderSystem.disableBlend();
//    }

}
