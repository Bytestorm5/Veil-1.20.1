package foundry.veil.forge.mixin.client.perspective.sodium;

import foundry.veil.forge.ext.SodiumWorldRendererExtension;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExtension {

    @Shadow
    private RenderSectionManager renderSectionManager;

    @Override
    public SortedRenderLists veil$getSortedRenderLists() {
        return this.renderSectionManager.getRenderLists();
    }

    @Override
    public Map<ChunkUpdateType, ArrayDeque<RenderSection>> veil$getRebuildLists() {
        return ((RenderSectionManagerAccessor) this.renderSectionManager).getRebuildLists();
    }

    @Override
    public void veil$setSortedRenderLists(SortedRenderLists sortedRenderLists) {
        ((RenderSectionManagerAccessor) this.renderSectionManager).setRenderLists(sortedRenderLists);
    }

    @Override
    public void veil$setRebuildLists(Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists) {
        ((RenderSectionManagerAccessor) this.renderSectionManager).setRebuildLists(rebuildLists);
    }
}
