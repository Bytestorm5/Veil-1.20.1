package foundry.veil.forge.mixin.client.perspective.sodium;

import foundry.veil.forge.ext.RenderRegionExtension;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExtension {

    @Unique
    private ChunkRenderList veil$perspectiveList;

    @Override
    public ChunkRenderList veil$getPerspectiveRenderList() {
        if (this.veil$perspectiveList == null) {
            this.veil$perspectiveList = new ChunkRenderList((RenderRegion) (Object) this);
        }
        return this.veil$perspectiveList;
    }
}
