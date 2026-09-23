package foundry.veil.forge.mixin.compat.sodium;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = SortedRenderLists.class, remap = false)
public interface SortedRenderListsAccessor {

    @Invoker("<init>")
    static SortedRenderLists init(ObjectArrayList<ChunkRenderList> lists) {
        throw new AssertionError();
    }
}
