package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import foundry.veil.impl.client.render.BackportRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ShaderInstance.class)
public abstract class PipelineShaderInstanceMixin {

    @Shadow
    @Nullable
    public Uniform MODEL_VIEW_MATRIX;

    @Unique
    private static final Matrix4f veil$MODEL_VIEW_MATRIX = new Matrix4f();

    // 1.20.1 has no ShaderInstance#setDefaultUniforms, and the default uniforms are always set right before the shader is applied.
    // Veil programs override apply, so ShaderProgramImpl.Wrapper sets these itself.
    @Inject(method = "apply", at = @At("HEAD"))
    public void setDefaultUniforms(CallbackInfo ci) {
        Matrix4f modelViewMatrix = this.MODEL_VIEW_MATRIX != null ? veil$MODEL_VIEW_MATRIX.set(this.MODEL_VIEW_MATRIX.getFloatBuffer()) : veil$MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
        BackportRenderHelper.setVeilDefaultUniforms((ShaderInstance) (Object) this, modelViewMatrix);
    }
}
