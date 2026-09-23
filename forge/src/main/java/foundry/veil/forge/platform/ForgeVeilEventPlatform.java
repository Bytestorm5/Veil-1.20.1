package foundry.veil.forge.platform;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.event.*;
import foundry.veil.forge.event.*;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Camera;
import foundry.veil.backport.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Map;

@SuppressWarnings({"Convert2MethodRef", "RedundantCast"})
@ApiStatus.Internal
public class ForgeVeilEventPlatform implements VeilEventPlatform {

    private static final BiMap<VeilRenderLevelStageEvent.Stage, RenderLevelStageEvent.Stage> STAGE_MAPPING = HashBiMap.create(Map.ofEntries(
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_SKY, RenderLevelStageEvent.Stage.AFTER_SKY),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS, RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS, RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS, RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_ENTITIES, RenderLevelStageEvent.Stage.AFTER_ENTITIES),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_PARTICLES, RenderLevelStageEvent.Stage.AFTER_PARTICLES),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_WEATHER, RenderLevelStageEvent.Stage.AFTER_WEATHER),
            Map.entry(VeilRenderLevelStageEvent.Stage.AFTER_LEVEL, RenderLevelStageEvent.Stage.AFTER_LEVEL)
    ));

    private IEventBus getModBus() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        if (!(container instanceof FMLModContainer fmlContainer)) {
            throw new IllegalStateException("Veil platform events must be registered from mod constructor");
        }
        return fmlContainer.getEventBus();
    }

    @Override
    public void onFreeNativeResources(FreeNativeResourcesEvent event) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ForgeFreeNativeResourcesEvent.class, forgeEvent -> event.onFree());
    }

    @Override
    public void onVeilAddShaderProcessors(VeilAddShaderPreProcessorsEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilAddShaderProcessorsEvent.class, forgeEvent -> event.onRegisterShaderPreProcessors(forgeEvent.getResourceProvider(), forgeEvent));
    }

    @Override
    public void preVeilPostProcessing(VeilPostProcessingEvent.Pre event) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ForgeVeilPostProcessingEvent.Pre.class, forgeEvent -> event.preVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline(), forgeEvent.getContext()));
    }

    @Override
    public void postVeilPostProcessing(VeilPostProcessingEvent.Post event) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ForgeVeilPostProcessingEvent.Post.class, forgeEvent -> event.postVeilPostProcessing(forgeEvent.getName(), forgeEvent.getPipeline(), forgeEvent.getContext()));
    }

    // This is needed for types to line up
    @Override
    public void onVeilRegisterBlockLayers(VeilRegisterBlockLayersEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilRegisterBlockLayersEvent.class, forgeEvent -> event.onRegisterBlockLayers((VeilRegisterBlockLayersEvent.Registry) forgeEvent));
    }

    @Override
    public void onVeilRegisterFixedBuffers(VeilRegisterFixedBuffersEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilRegisterFixedBuffersEvent.class, forgeEvent -> event.onRegisterFixedBuffers((stage, renderType) -> {
            if (stage == null) {
                forgeEvent.register(null, renderType);
                return;
            }

            RenderLevelStageEvent.Stage forgeStage = getForgeStage(stage);
            if (forgeStage != null) {
                forgeEvent.register(forgeStage, renderType);
            }
        }));
    }

    @Override
    public void onVeilRegisterGlobalControllers(VeilRegisterGlobalControllersEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilRegisterGlobalControllersEvent.class, forgeEvent -> event.onRegisterGlobalControllers((VeilRegisterGlobalControllersEvent.Registry) forgeEvent));
    }

    @Override
    public void onVeilRegisterInspectors(VeilRegisterInspectorsEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilRegisterInspectorsEvent.class, forgeEvent -> event.onRegisterInspectors((VeilRegisterInspectorsEvent.Registry) forgeEvent));
    }

    @Override
    public void onVeilRendererAvailable(VeilRendererAvailableEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilRendererAvailableEvent.class, forgeEvent -> event.onVeilRendererAvailable(forgeEvent.getRenderer()));
    }

    @Override
    public void onVeilRenderLevelStage(VeilRenderLevelStageEvent event) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderLevelStageEvent.class, forgeEvent -> {
            VeilRenderLevelStageEvent.Stage stage = getVeilStage(forgeEvent.getStage());
            if (stage == null) {
                return;
            }

            LevelRenderer levelRenderer = forgeEvent.getLevelRenderer();
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            MatrixStack poseStack = VeilRenderBridge.create(forgeEvent.getPoseStack());
            Matrix4f modelViewMatrix = forgeEvent.getPoseStack().last().pose();
            Matrix4f projectionMatrix = forgeEvent.getProjectionMatrix();
            int renderTick = forgeEvent.getRenderTick();
            DeltaTracker deltaTracker = DeltaTracker.of(Minecraft.getInstance().getDeltaFrameTime(), forgeEvent.getPartialTick());
            Camera camera = forgeEvent.getCamera();
            Frustum frustum = forgeEvent.getFrustum();
            event.onRenderLevelStage(stage, levelRenderer, bufferSource, poseStack, modelViewMatrix, projectionMatrix, renderTick, deltaTracker, camera, frustum);
        });
    }

    @Override
    public void onVeilShaderCompile(VeilShaderCompileEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilShaderCompileEvent.class, forgeEvent -> event.onVeilCompileShaders(forgeEvent.getShaderManager(), forgeEvent.getUpdatedPrograms()));
    }

    @Override
    public void onVeilDynamicBuffersChanged(VeilDynamicBuffersChangedEvent event) {
        this.getModBus().addListener(EventPriority.NORMAL, false, ForgeVeilDynamicBuffersChangedEvent.class, forgeEvent -> event.onVeilDynamicBuffersChanged(forgeEvent.getChange()));
    }

    public static @Nullable RenderLevelStageEvent.Stage getForgeStage(VeilRenderLevelStageEvent.Stage stage) {
        return STAGE_MAPPING.get(stage);
    }

    public static @Nullable VeilRenderLevelStageEvent.Stage getVeilStage(RenderLevelStageEvent.Stage stage) {
        return STAGE_MAPPING.inverse().get(stage);
    }
}
