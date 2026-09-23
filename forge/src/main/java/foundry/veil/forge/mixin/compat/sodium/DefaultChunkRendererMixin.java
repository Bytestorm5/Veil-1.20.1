package foundry.veil.forge.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import foundry.veil.forge.compat.sodium.VeilChunkVertex;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

/**
 * Embeddium hardcodes the four compact vertex attribute bindings, so the extra Veil normal attribute is appended here.
 */
@Mixin(value = DefaultChunkRenderer.class, remap = false)
public abstract class DefaultChunkRendererMixin extends ShaderChunkRenderer {

    public DefaultChunkRendererMixin(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }

    @ModifyReturnValue(method = "getBindingsForType", at = @At("RETURN"))
    private GlVertexAttributeBinding[] addNormalBinding(GlVertexAttributeBinding[] original) {
        if (!(this.vertexType instanceof VeilChunkVertex) || original == null) {
            return original;
        }
        for (GlVertexAttributeBinding binding : original) {
            if (binding.getIndex() == VeilChunkVertex.NORMAL_ATTRIBUTE_INDEX) {
                return original;
            }
        }
        GlVertexAttributeBinding[] bindings = Arrays.copyOf(original, original.length + 1);
        bindings[original.length] = new GlVertexAttributeBinding(VeilChunkVertex.NORMAL_ATTRIBUTE_INDEX, VeilChunkVertex.NORMAL_ATTRIBUTE);
        return bindings;
    }
}
