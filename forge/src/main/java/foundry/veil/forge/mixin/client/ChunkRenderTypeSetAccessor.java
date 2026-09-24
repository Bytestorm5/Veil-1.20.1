package foundry.veil.forge.mixin.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;
import java.util.List;

// Applied after Embeddium's ChunkRenderTypeSetMixin (priority 1000) so VeilMixinPluginImpl can make its lookup table mutable
@Mixin(value = ChunkRenderTypeSet.class, priority = 1100, remap = false)
public interface ChunkRenderTypeSetAccessor {

    @Mutable
    @Accessor("CHUNK_RENDER_TYPES_LIST")
    static void veil$setChunkRenderTypesList(List<RenderType> chunkRenderTypes) {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("CHUNK_RENDER_TYPES")
    static void veil$setChunkRenderTypes(RenderType[] chunkRenderTypes) {
        throw new AssertionError();
    }

    @Invoker("<init>")
    static ChunkRenderTypeSet veil$create(BitSet bits) {
        throw new AssertionError();
    }

    @Accessor("bits")
    BitSet veil$getBits();
}
