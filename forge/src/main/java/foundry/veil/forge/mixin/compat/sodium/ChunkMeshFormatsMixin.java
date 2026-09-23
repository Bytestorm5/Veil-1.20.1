package foundry.veil.forge.mixin.compat.sodium;

import foundry.veil.forge.compat.sodium.VeilChunkVertex;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkMeshFormats.class, remap = false)
public class ChunkMeshFormatsMixin {

    @Mutable
    @Shadow
    @Final
    public static ChunkVertexType COMPACT;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clinit(CallbackInfo ci) {
        COMPACT = new VeilChunkVertex();
    }
}
