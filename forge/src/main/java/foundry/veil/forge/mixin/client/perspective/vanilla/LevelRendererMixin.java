package foundry.veil.forge.mixin.client.perspective.vanilla;

import foundry.veil.impl.client.render.perspective.LevelPerspectiveCamera;
import foundry.veil.impl.client.render.perspective.VeilSectionOcclusionGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Nullable
    private ChunkRenderDispatcher chunkRenderDispatcher;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

    @Shadow
    @Nullable
    private ViewArea viewArea;

    @Unique
    private final VeilSectionOcclusionGraph veil$perspectiveOcclusionGraph = new VeilSectionOcclusionGraph();
    @Unique
    private final List<ChunkRenderDispatcher.RenderChunk> veil$visibleSections = new ObjectArrayList<>(10000);
    @Unique
    private final ObjectArrayList<LevelRenderer.RenderChunkInfo> veil$backupVisibleSections = new ObjectArrayList<>(10000);
    @Unique
    private boolean veil$swappedSections;

    @Inject(method = "setupRender", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", shift = At.Shift.AFTER, args = "ldc=camera"), cancellable = true)
    public void setupRender(Camera camera, Frustum frustum, boolean hasCapturedFrustum, boolean isSpectator, CallbackInfo ci) {
        if (!(camera instanceof LevelPerspectiveCamera perspectiveCamera)) {
            return;
        }

        ci.cancel();

        Entity.setViewScale(Mth.clamp(perspectiveCamera.getRenderDistance() / 8.0, 1.0, 2.5));
        Objects.requireNonNull(this.chunkRenderDispatcher).setCamera(camera.getPosition());
        ProfilerFiller profiler = this.minecraft.getProfiler();
        profiler.push("veil_section_occlusion_graph");
        this.veil$visibleSections.clear();
        this.veil$perspectiveOcclusionGraph.update(Objects.requireNonNull(this.viewArea), this.minecraft.smartCull, perspectiveCamera, frustum, this.veil$visibleSections);
        profiler.pop();
        profiler.pop();

        // The vanilla list is final on 1.20.1, so swap its contents instead of the list
        if (!this.veil$swappedSections) {
            this.veil$backupVisibleSections.clear();
            this.veil$backupVisibleSections.addAll(this.renderChunksInFrustum);
            this.veil$swappedSections = true;
        }
        this.renderChunksInFrustum.clear();
        for (ChunkRenderDispatcher.RenderChunk section : this.veil$visibleSections) {
            this.renderChunksInFrustum.add(new LevelRenderer.RenderChunkInfo(section, null, 0));
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    public void resetSections(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (camera instanceof LevelPerspectiveCamera) {
            Entity.setViewScale(Mth.clamp((double) this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get());
            if (this.veil$swappedSections) {
                this.renderChunksInFrustum.clear();
                this.renderChunksInFrustum.addAll(this.veil$backupVisibleSections);
                this.veil$backupVisibleSections.clear();
                this.veil$swappedSections = false;
            }
            this.veil$visibleSections.clear();
        }
    }
}
