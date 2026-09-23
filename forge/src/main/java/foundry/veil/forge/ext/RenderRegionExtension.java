package foundry.veil.forge.ext;

import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;

public interface RenderRegionExtension {

    ChunkRenderList veil$getPerspectiveRenderList();
}
