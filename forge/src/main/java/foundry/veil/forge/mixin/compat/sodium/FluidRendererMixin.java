package foundry.veil.forge.mixin.compat.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.veil.ext.sodium.ChunkVertexEncoderVertexExtension;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidRenderer.class, remap = false)
public class FluidRendererMixin {

    @Inject(method = "writeQuad", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;getTexV(I)F"))
    public void bufferNormal(ChunkModelBuilder builder, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip, CallbackInfo ci, @Local ChunkVertexEncoder.Vertex out) {
        ((ChunkVertexEncoderVertexExtension) out).veil$setNormal(quad.getComputedFaceNormal());
    }
}
