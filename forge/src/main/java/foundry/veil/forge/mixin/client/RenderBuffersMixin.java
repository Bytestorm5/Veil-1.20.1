package foundry.veil.forge.mixin.client;

import foundry.veil.Veil;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.forge.event.ForgeVeilRegisterBlockLayersEvent;
import foundry.veil.forge.impl.ForgeChunkRenderTypeSetSync;
import foundry.veil.forge.impl.ForgeRenderTypeStageHandler;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {

    // The fixed buffer pack allocates a buffer for every chunk layer, so custom block layers have to be registered first.
    // Mixin 0.8.5 only allows RETURN injections in constructors, so wrap the pack's creation instead.
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "()Lnet/minecraft/client/renderer/ChunkBufferBuilderPack;"))
    private ChunkBufferBuilderPack registerBlockLayers() {
        Set<RenderType> blockLayers = new HashSet<>();
        ModLoader.get().postEvent(new ForgeVeilRegisterBlockLayersEvent(renderType -> {
            if (Veil.platform().isDevelopmentEnvironment() && renderType.bufferSize() > RenderType.SMALL_BUFFER_SIZE) {
                Veil.LOGGER.warn("Block render layer '{}' uses a large buffer size: {}. If this is intended you can ignore this message", VeilRenderType.getName(renderType), renderType.bufferSize());
            }
            blockLayers.add(renderType);
        }));
        ForgeRenderTypeStageHandler.setBlockLayers(blockLayers);
        ForgeChunkRenderTypeSetSync.sync();
        return new ChunkBufferBuilderPack();
    }
}
