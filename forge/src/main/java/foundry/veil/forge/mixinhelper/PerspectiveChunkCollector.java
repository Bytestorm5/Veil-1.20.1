package foundry.veil.forge.mixinhelper;

import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.forge.ext.RenderRegionExtension;
import foundry.veil.forge.mixin.compat.sodium.SortedRenderListsAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;

/**
 * Perspective equivalent of Embeddium's {@code VisibleChunkCollector}. Collects sections into a separate per-region
 * render list so the main camera's render lists are left untouched while a perspective is drawn.
 */
public class PerspectiveChunkCollector implements OcclusionCuller.Visitor {

    private final ObjectArrayList<ChunkRenderList> renderLists;
    private final EnumMap<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists;

    public PerspectiveChunkCollector() {
        this.renderLists = new ObjectArrayList<>();
        this.rebuildLists = new EnumMap<>(ChunkUpdateType.class);
        for (ChunkUpdateType type : ChunkUpdateType.values()) {
            this.rebuildLists.put(type, new ArrayDeque<>());
        }
    }

    @Override
    public void visit(RenderSection section, boolean visible) {
        RenderRegion region = section.getRegion();
        ChunkRenderList renderList = ((RenderRegionExtension) region).veil$getPerspectiveRenderList();

        int id = VeilLevelPerspectiveRenderer.getID();
        if (renderList.getLastVisibleFrame() != id) {
            renderList.reset(id);
            this.renderLists.add(renderList);
        }

        if (visible && section.getFlags() != 0) {
            renderList.add(section);
        }

        ChunkUpdateType type = section.getPendingUpdate();
        if (type != null && section.getBuildCancellationToken() == null) {
            Queue<RenderSection> queue = this.rebuildLists.get(type);
            if (queue.size() < type.getMaximumQueueSize()) {
                queue.add(section);
            }
        }
    }

    public SortedRenderLists createRenderLists() {
        return SortedRenderListsAccessor.init(this.renderLists);
    }

    public Map<ChunkUpdateType, ArrayDeque<RenderSection>> getRebuildLists() {
        return this.rebuildLists;
    }
}
