package foundry.veil.forge.mixin.client;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.class)
public interface ForgeRenderTypeAccessor {

    // Added by Forge, so it isn't remapped
    @Accessor(value = "chunkLayerId", remap = false)
    void veil$setChunkLayerId(int chunkLayerId);
}
