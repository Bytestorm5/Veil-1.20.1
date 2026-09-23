package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(ShaderInstance.class)
public abstract class PipelineShaderInstanceMixin {

    @Unique
    private static final Direction[] veil$DIRECTIONS = Direction.values();
    @Unique
    private static final String[] veil$FACE_BRIGHTNESS_UNIFORM_NAMES = Arrays.stream(veil$DIRECTIONS)
            .map(direction -> "VeilBlockFaceBrightness[" + direction.get3DDataValue() + "]")
            .toArray(String[]::new);
    @Unique
    private static final Matrix3f veil$NORMAL_MATRIX = new Matrix3f();

    @Shadow
    @Nullable
    public abstract Uniform getUniform(String name);

    @Shadow
    @Nullable
    public Uniform MODEL_VIEW_MATRIX;

    @Unique
    private static final Matrix4f veil$MODEL_VIEW_MATRIX = new Matrix4f();

    // 1.20.1 has no ShaderInstance#setDefaultUniforms, and the default uniforms are always set right before the shader is applied
    @Inject(method = "apply", at = @At("HEAD"))
    public void setDefaultUniforms(CallbackInfo ci) {
        Uniform renderTime = this.getUniform("VeilRenderTime");
        if (renderTime != null) {
            renderTime.set((System.currentTimeMillis() % 3_600_000) / 1000.0F);
        }

        Uniform normalMat = this.getUniform("NormalMat");
        if (normalMat != null) {
            Matrix4f modelViewMatrix = this.MODEL_VIEW_MATRIX != null ? veil$MODEL_VIEW_MATRIX.set(this.MODEL_VIEW_MATRIX.getFloatBuffer()) : veil$MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
            normalMat.set(modelViewMatrix.normal(veil$NORMAL_MATRIX));
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Direction value : veil$DIRECTIONS) {
                Uniform uniform = this.getUniform(veil$FACE_BRIGHTNESS_UNIFORM_NAMES[value.ordinal()]);
                if (uniform != null) {
                    uniform.set(level.getShade(value, true));
                }
            }
        }
    }
}
