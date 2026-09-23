package foundry.veil.forge.compat.sodium;

import foundry.veil.api.compat.SodiumCompat;
import foundry.veil.forge.ext.ShaderChunkRendererExtension;
import foundry.veil.forge.ext.SodiumWorldRendererExtension;
import foundry.veil.forge.mixin.compat.sodium.RenderSectionManagerAccessor;
import foundry.veil.forge.mixin.compat.sodium.SodiumWorldRendererAccessor;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;

/**
 * Veil compat for Embeddium (the Forge 1.20.1 fork of Sodium 0.5).
 * <p>
 * The opaque objects exchanged through {@link #getSortedRenderLists()} and {@link #getTaskLists()} are Embeddium's
 * {@link SortedRenderLists} and its rebuild queues ({@code Map<ChunkUpdateType, ArrayDeque<RenderSection>>}).
 */
public class VeilForgeSodiumCompat implements SodiumCompat {

    private static @NotNull StringBuilder getShaderName(ChunkShaderOptions options) {
        StringBuilder name = new StringBuilder("chunk_shader");
        if (options.fog() == ChunkFogMode.SMOOTH) {
            name.append("_fog_smooth");
        }

        TerrainRenderPass pass = options.pass();
        if (pass.isReverseOrder()) {
            name.append("_translucent");
        }
        if (pass.supportsFragmentDiscard()) {
            name.append("_cutout");
        }
        return name;
    }

    private static Map<ChunkUpdateType, ArrayDeque<RenderSection>> createEmptyRebuildLists() {
        Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists = new EnumMap<>(ChunkUpdateType.class);
        for (ChunkUpdateType type : ChunkUpdateType.values()) {
            rebuildLists.put(type, new ArrayDeque<>());
        }
        return rebuildLists;
    }

    private static @Nullable ShaderChunkRendererExtension getChunkRenderer() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) worldRenderer).getRenderSectionManager();
            if (renderSectionManager != null && ((RenderSectionManagerAccessor) renderSectionManager).getChunkRenderer() instanceof ShaderChunkRendererExtension extension) {
                return extension;
            }
        }
        return null;
    }

    @Override
    public Object2IntMap<ResourceLocation> getLoadedShaders() {
        ShaderChunkRendererExtension extension = getChunkRenderer();
        if (extension != null) {
            Object2IntMap<ResourceLocation> shaders = new Object2IntArrayMap<>(extension.veil$getPrograms().size());
            for (Map.Entry<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> entry : extension.veil$getPrograms().entrySet()) {
                StringBuilder name = getShaderName(entry.getKey());
                shaders.put(new ResourceLocation("sodium", name.toString()), entry.getValue().handle());
            }
            return shaders;
        }
        return Object2IntMaps.emptyMap();
    }

    @Override
    public void recompile() {
        ShaderChunkRendererExtension extension = getChunkRenderer();
        if (extension != null) {
            extension.veil$recompile();
        }
    }

    @Override
    public void setActiveBuffers(int activeBuffers) {
        ShaderChunkRendererExtension extension = getChunkRenderer();
        if (extension != null) {
            extension.veil$setActiveBuffers(activeBuffers);
        }
    }

    @Override
    public void markChunksDirty() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            RenderSectionManager renderSectionManager = ((SodiumWorldRendererAccessor) worldRenderer).getRenderSectionManager();
            if (renderSectionManager != null) {
                Long2ReferenceMap<RenderSection> map = ((RenderSectionManagerAccessor) renderSectionManager).getSectionByPosition();
                // Copy the keys first since scheduling a rebuild can modify the section map
                LongArrayList positions = new LongArrayList(map.keySet());
                for (LongIterator iterator = positions.iterator(); iterator.hasNext(); ) {
                    long sectionPos = iterator.nextLong();
                    renderSectionManager.scheduleRebuild(SectionPos.x(sectionPos), SectionPos.y(sectionPos), SectionPos.z(sectionPos), true);
                }
            }
        }
    }

    @Override
    public Object getSortedRenderLists() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer == null) {
            return SortedRenderLists.empty();
        }
        return ((SodiumWorldRendererExtension) worldRenderer).veil$getSortedRenderLists();
    }

    @Override
    public void setSortedRenderLists(@Nullable Object sortedRenderLists) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            SortedRenderLists renderLists = sortedRenderLists != null ? (SortedRenderLists) sortedRenderLists : SortedRenderLists.empty();
            ((SodiumWorldRendererExtension) worldRenderer).veil$setSortedRenderLists(renderLists);
        }
    }

    @Override
    public Object getTaskLists() {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            return ((SodiumWorldRendererExtension) worldRenderer).veil$getRebuildLists();
        }
        return createEmptyRebuildLists();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setTaskList(@Nullable Object taskList) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer != null) {
            Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists = taskList != null ? (Map<ChunkUpdateType, ArrayDeque<RenderSection>>) taskList : createEmptyRebuildLists();
            ((SodiumWorldRendererExtension) worldRenderer).veil$setRebuildLists(rebuildLists);
        }
    }
}
