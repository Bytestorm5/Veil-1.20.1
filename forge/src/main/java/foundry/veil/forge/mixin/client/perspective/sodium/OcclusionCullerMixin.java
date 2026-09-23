package foundry.veil.forge.mixin.client.perspective.sodium;

import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.forge.ext.RenderSectionExtension;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.VisibilityEncoding;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.util.collections.WriteQueue;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks visited sections with the perspective ID instead of the section's last visible frame while rendering a
 * perspective, so the main camera's visibility state is left untouched.
 */
@Mixin(value = OcclusionCuller.class, remap = false)
public abstract class OcclusionCullerMixin {

    @Shadow
    protected abstract RenderSection getRenderSection(int x, int y, int z);

    @Shadow
    private static void visitNeighbors(WriteQueue<RenderSection> queue, RenderSection section, int outgoing, int frame) {
        throw new AssertionError();
    }

    @Inject(method = "visitNode", at = @At("HEAD"), cancellable = true)
    private static void visitNode(WriteQueue<RenderSection> queue, @NotNull RenderSection render, int incoming, int frame, CallbackInfo ci) {
        if (!VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }

        ci.cancel();
        RenderSectionExtension ext = (RenderSectionExtension) render;
        if (ext.veil$hasNotRendered()) {
            ext.veil$markRendered();
            queue.enqueue(render);
        }
        ext.veil$addIncomingDirections(incoming);
    }

    @Inject(method = "initWithinWorld", at = @At("HEAD"), cancellable = true)
    public void initWithinWorld(OcclusionCuller.Visitor visitor, WriteQueue<RenderSection> queue, Viewport viewport, boolean useOcclusionCulling, int frame, CallbackInfo ci) {
        if (!VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }

        ci.cancel();
        SectionPos origin = viewport.getChunkCoord();
        RenderSection section = this.getRenderSection(origin.getX(), origin.getY(), origin.getZ());
        if (section == null) {
            return;
        }

        ((RenderSectionExtension) section).veil$markRendered();
        visitor.visit(section, true);

        int outgoing;
        if (useOcclusionCulling) {
            outgoing = VisibilityEncoding.getConnections(section.getVisibilityData());
        } else {
            outgoing = 0b111111;
        }

        visitNeighbors(queue, section, outgoing, frame);
    }
}
