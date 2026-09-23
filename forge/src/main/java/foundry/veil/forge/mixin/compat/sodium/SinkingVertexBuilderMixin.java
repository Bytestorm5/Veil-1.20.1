package foundry.veil.forge.mixin.compat.sodium;

import foundry.veil.ext.sodium.ChunkVertexEncoderVertexExtension;
import me.jellysquid.mods.sodium.client.compat.ccl.SinkingVertexBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Embeddium's buffer for custom (vanilla {@code VertexConsumer}) block renderers does not keep per-vertex normals, so
 * the flat normal of each quad is computed from its positions instead.
 */
@Mixin(value = SinkingVertexBuilder.class, remap = false)
public class SinkingVertexBuilderMixin {

    @Shadow
    @Final
    private ChunkVertexEncoder.Vertex[] sodiumVertexArray;

    @Inject(method = "flush(Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;FFF)Z", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;)V"))
    public void bufferNormal(ChunkModelBuilder buffers, Material material, float oX, float oY, float oZ, CallbackInfoReturnable<Boolean> cir) {
        ChunkVertexEncoder.Vertex[] vertices = this.sodiumVertexArray;
        ChunkVertexEncoder.Vertex v0 = vertices[0];
        ChunkVertexEncoder.Vertex v1 = vertices[1];
        ChunkVertexEncoder.Vertex v2 = vertices[2];
        ChunkVertexEncoder.Vertex v3 = vertices[3];

        float dx0 = v2.x - v0.x;
        float dy0 = v2.y - v0.y;
        float dz0 = v2.z - v0.z;
        float dx1 = v3.x - v1.x;
        float dy1 = v3.y - v1.y;
        float dz1 = v3.z - v1.z;

        float normX = dy0 * dz1 - dz0 * dy1;
        float normY = dz0 * dx1 - dx0 * dz1;
        float normZ = dx0 * dy1 - dy0 * dx1;
        float length = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);

        int packedNormal = 0;
        if (length > 1.0E-6F) {
            packedNormal = NormI8.pack(normX / length, normY / length, normZ / length);
        }
        for (ChunkVertexEncoder.Vertex vertex : vertices) {
            ((ChunkVertexEncoderVertexExtension) vertex).veil$setNormal(packedNormal);
        }
    }
}
