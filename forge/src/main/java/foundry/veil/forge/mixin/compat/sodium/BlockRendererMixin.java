package foundry.veil.forge.mixin.compat.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.ext.sodium.ChunkVertexEncoderVertexExtension;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public class BlockRendererMixin {

    @Shadow
    @Final
    private ChunkVertexEncoder.Vertex[] vertices;

    @Inject(method = "writeGeometry", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;getLight(I)I"))
    public void bufferNormal(BlockRenderContext ctx, ChunkModelBuilder builder, Vec3 offset, Material material, BakedQuadView quad, int[] colors, QuadLightData light, CallbackInfo ci, @Local(ordinal = 0) int dstIndex, @Local(ordinal = 1) int srcIndex) {
        ChunkVertexEncoder.Vertex out = this.vertices[dstIndex];
        // Forge models can carry per-vertex normals, so fall back to the computed face normal when they are absent
        int packedNormal = quad.getForgeNormal(srcIndex);
        if ((packedNormal & 0xFFFFFF) == 0) {
            packedNormal = quad.getComputedFaceNormal();
        }
        ((ChunkVertexEncoderVertexExtension) out).veil$setNormal(packedNormal);
    }
}
