package foundry.veil.forge.mixin.compat.sodium;

import foundry.veil.ext.sodium.ChunkVertexEncoderVertexExtension;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ChunkVertexEncoder.Vertex.class, remap = false)
public class ChunkVertexEncoderVertexMixin implements ChunkVertexEncoderVertexExtension {

    @Unique
    private int veil$packedNormal;

    @Override
    public int veil$getPackedNormal() {
        return this.veil$packedNormal;
    }

    @Override
    public void veil$setNormal(int packedNormal) {
        this.veil$packedNormal = packedNormal;
    }
}
