package foundry.veil.forge.ext;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;

import java.util.ArrayDeque;
import java.util.Map;

public interface SodiumWorldRendererExtension {

    SortedRenderLists veil$getSortedRenderLists();

    Map<ChunkUpdateType, ArrayDeque<RenderSection>> veil$getRebuildLists();

    void veil$setSortedRenderLists(SortedRenderLists sortedRenderLists);

    void veil$setRebuildLists(Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists);
}
