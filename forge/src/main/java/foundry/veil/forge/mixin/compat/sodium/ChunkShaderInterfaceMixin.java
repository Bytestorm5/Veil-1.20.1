package foundry.veil.forge.mixin.compat.sodium;

import foundry.veil.forge.compat.sodium.VeilNormalUniform;
import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import org.joml.Matrix3f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL20C.glGetUniformLocation;

@Mixin(value = ChunkShaderInterface.class, remap = false)
public class ChunkShaderInterfaceMixin {

    @Unique
    private VeilNormalUniform veil$uniformNormalMatrix;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
        // Embeddium has no optional uniform binding and throws when a uniform is missing, so look it up directly
        if (context instanceof GlObject program) {
            int location = glGetUniformLocation(program.handle(), "VeilNormalMatrix");
            this.veil$uniformNormalMatrix = location >= 0 ? new VeilNormalUniform(location) : null;
        } else {
            try {
                this.veil$uniformNormalMatrix = context.bindUniform("VeilNormalMatrix", VeilNormalUniform::new);
            } catch (NullPointerException ignored) {
                this.veil$uniformNormalMatrix = null;
            }
        }
    }

    @Inject(method = "setModelViewMatrix", at = @At("TAIL"))
    public void setModelViewMatrix(Matrix4fc matrix, CallbackInfo ci) {
        if (this.veil$uniformNormalMatrix != null) {
            this.veil$uniformNormalMatrix.set(matrix.normal(new Matrix3f()));
        }
    }
}
