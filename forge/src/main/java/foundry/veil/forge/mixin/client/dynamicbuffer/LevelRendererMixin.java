package foundry.veil.forge.mixin.client.dynamicbuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.dynamicbuffer.DynamicBufferShard;
import net.minecraft.client.renderer.LevelRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Nullable
    public abstract RenderTarget getParticlesTarget();

    @Unique
    private final DynamicBufferShard veil$particleBufferShard = new DynamicBufferShard("particles", this::getParticlesTarget);

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;dispatchRenderStage(Lnet/minecraftforge/client/event/RenderLevelStageEvent$Stage;Lnet/minecraft/client/renderer/LevelRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;ILnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;)V", ordinal = 2, shift = At.Shift.BEFORE, remap = false))
    public void beginTranslucent(CallbackInfo ci) {
        VeilRenderSystem.renderer().getDynamicBufferManger().setEnabled(true);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", remap = false, shift = At.Shift.BEFORE))
    public void preRenderParticles(CallbackInfo ci) {
        this.veil$particleBufferShard.setupRenderState();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", remap = false, shift = At.Shift.AFTER))
    public void postRenderParticles(CallbackInfo ci) {
        this.veil$particleBufferShard.clearRenderState();
    }
}
