package foundry.veil.forge.mixin.client.perspective.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(value = RenderSectionManager.class, remap = false)
public interface RenderSectionManagerAccessor {

    @Accessor
    Map<ChunkUpdateType, ArrayDeque<RenderSection>> getRebuildLists();

    @Accessor
    void setRenderLists(SortedRenderLists renderLists);

    @Accessor
    void setRebuildLists(Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists);
}
