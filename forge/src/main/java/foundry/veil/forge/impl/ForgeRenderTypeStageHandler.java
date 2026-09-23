package foundry.veil.forge.impl;

import com.google.common.collect.ImmutableList;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.ext.LevelRendererBlockLayerExtension;
import foundry.veil.forge.platform.ForgeVeilEventPlatform;
import foundry.veil.forge.mixin.client.ForgeRenderTypeAccessor;
import foundry.veil.mixin.rendertype.accessor.RenderTypeBufferSourceAccessor;
import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = Veil.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeRenderTypeStageHandler {

    private static final Map<RenderLevelStageEvent.Stage, Set<RenderType>> STAGE_RENDER_TYPES = new HashMap<>();
    private static Set<RenderType> CUSTOM_BLOCK_LAYERS = Set.of();
    private static List<RenderType> BLOCK_LAYERS;

    public static synchronized void register(@Nullable RenderLevelStageEvent.Stage stage, RenderType renderType) {
        Map<RenderType, BufferBuilder> fixedBuffers = ((RenderTypeBufferSourceAccessor) Minecraft.getInstance().renderBuffers().bufferSource()).getFixedBuffers();
        fixedBuffers.put(renderType, new BufferBuilder(renderType.bufferSize()));

        if (stage != null) {
            STAGE_RENDER_TYPES.computeIfAbsent(stage, unused -> new ObjectArraySet<>()).add(renderType);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStageEnd(RenderLevelStageEvent event) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        RenderLevelStageEvent.Stage stage = event.getStage();

        Set<RenderType> stages = STAGE_RENDER_TYPES.get(stage);
        if (stages != null) {
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            stages.forEach(renderType -> {
                profiler.push("render_" + VeilRenderType.getName(renderType));
                if (CUSTOM_BLOCK_LAYERS.contains(renderType)) {
                    Vec3 pos = event.getCamera().getPosition();
                    ((LevelRendererBlockLayerExtension) event.getLevelRenderer()).veil$drawBlockLayer(renderType, pos.x, pos.y, pos.z, event.getPoseStack().last().pose(), event.getProjectionMatrix());
                }
                bufferSource.endBatch(renderType);
                profiler.pop();
            });
        }

        if (!VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            VeilRenderLevelStageEvent.Stage veilStage = ForgeVeilEventPlatform.getVeilStage(stage);
            if (veilStage != null) {
                profiler.push("post");
                VeilRenderSystem.renderPost(veilStage);
                profiler.pop();
            }
        }
    }

    // Some mods add custom block layers by changing the field, so account for that
    public static List<RenderType> getBlockLayers(List<RenderType> base) {
        if (CUSTOM_BLOCK_LAYERS.isEmpty()) {
            return base;
        }

        if (BLOCK_LAYERS == null || base.size() != BLOCK_LAYERS.size()) {
            ImmutableList.Builder<RenderType> blockLayers = ImmutableList.builder();
            blockLayers.addAll(base);
            if (CUSTOM_BLOCK_LAYERS != null) {
                blockLayers.addAll(CUSTOM_BLOCK_LAYERS);

                // Assign Forge chunk layer ids
                int i = base.get(base.size() - 1).getChunkLayerId();
                for (RenderType blockLayer : CUSTOM_BLOCK_LAYERS) {
                    ((ForgeRenderTypeAccessor) blockLayer).veil$setChunkLayerId(++i);
                }
            }
            BLOCK_LAYERS = blockLayers.build();
        }
        return BLOCK_LAYERS;
    }

    public static void setBlockLayers(Set<RenderType> blockLayers) {
        CUSTOM_BLOCK_LAYERS = Set.copyOf(blockLayers);
        BLOCK_LAYERS = null;
    }
}
