package foundry.veil.forge.mixin.client.perspective.sodium;

import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.forge.ext.RenderSectionExtension;
import foundry.veil.forge.mixinhelper.PerspectiveChunkCollector;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class RenderSectionManagerMixin {

    @Shadow
    private Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists;

    @Shadow
    private SortedRenderLists renderLists;

    @Shadow
    @Final
    private OcclusionCuller occlusionCuller;

    @Shadow
    protected abstract boolean shouldUseOcclusionCulling(Camera camera, boolean spectator);

    @Shadow
    protected abstract float getSearchDistance();

    @Shadow
    protected abstract void resetRenderLists();

    @Shadow
    protected abstract RenderSection getRenderSection(int x, int y, int z);

    @Inject(method = "createTerrainRenderList", at = @At("HEAD"), cancellable = true)
    private void createTerrainRenderList(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
        if (!VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }

        ci.cancel();
        this.resetRenderLists();

        float searchDistance = this.getSearchDistance();
        boolean useOcclusionCulling = this.shouldUseOcclusionCulling(camera, spectator);

        PerspectiveChunkCollector visitor = new PerspectiveChunkCollector();
        this.occlusionCuller.findVisible(visitor, viewport, searchDistance, useOcclusionCulling, frame);

        this.renderLists = visitor.createRenderLists();
        this.rebuildLists = visitor.getRebuildLists();
    }

    @Inject(method = "isSectionVisible", at = @At("HEAD"), cancellable = true)
    public void isSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (!VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }

        RenderSection render = this.getRenderSection(x, y, z);
        cir.setReturnValue(render != null && !((RenderSectionExtension) render).veil$hasNotRendered());
    }
}
