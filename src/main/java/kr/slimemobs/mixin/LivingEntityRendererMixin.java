package kr.slimemobs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "scale", at = @At("TAIL"))
    private void slimeMobs$squashAndStretch(LivingEntityRenderState state, PoseStack poseStack, CallbackInfo ci) {
        // A soft slime pulse synchronized to the hop rhythm.
        // X/Z widen while Y compresses, then Y stretches while X/Z narrow.
        float wave = (float)Math.sin(state.ageInTicks * 0.55F);
        float yScale = 1.0F + wave * 0.13F;
        float xzScale = 1.0F - wave * 0.065F;

        // Stronger compression near the bottom of the cycle for a bouncy landing feel.
        float landing = Math.max(0.0F, (float)Math.sin(state.ageInTicks * 0.55F + Math.PI));
        yScale -= landing * 0.08F;
        xzScale += landing * 0.04F;

        poseStack.scale(xzScale, yScale, xzScale);
    }
}
