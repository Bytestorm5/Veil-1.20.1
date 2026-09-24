package foundry.veil.forge.impl;

import foundry.veil.Veil;
import foundry.veil.forge.mixin.client.ChunkRenderTypeSetAccessor;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Keeps {@link ChunkRenderTypeSet} in sync with the chunk layers once Veil's custom block layers are registered.
 * <p>
 * Forge computes the chunk layers when {@link ChunkRenderTypeSet} is initialized, which can happen before the custom
 * layers exist. Embeddium replaces the set with a mask into a table sized for the layers that existed at that point, so
 * sets containing a custom layer would index past the end of it.
 */
@ApiStatus.Internal
public final class ForgeChunkRenderTypeSetSync {

    // Embeddium only reads the first byte of the bit set
    private static final int EMBEDDIUM_MAX_LAYERS = 8;

    private ForgeChunkRenderTypeSetSync() {
    }

    public static void sync() {
        List<RenderType> layers = RenderType.chunkBufferLayers();
        ChunkRenderTypeSetAccessor.veil$setChunkRenderTypesList(layers);
        ChunkRenderTypeSetAccessor.veil$setChunkRenderTypes(layers.toArray(new RenderType[0]));

        ChunkRenderTypeSet all = ChunkRenderTypeSet.all();
        ((ChunkRenderTypeSetAccessor) (Object) all).veil$getBits().set(0, layers.size());

        if (Veil.SODIUM) {
            syncEmbeddium(layers.size(), all);
        }
    }

    private static void syncEmbeddium(int layerCount, ChunkRenderTypeSet all) {
        Field universeField;
        Field maskAllField;
        Field combinationsField;
        Field maskField;
        try {
            universeField = field("UNIVERSE");
            maskAllField = field("MASK_ALL");
            combinationsField = field("POSSIBLE_RENDER_TYPE_COMBINATIONS");
            maskField = field("mask");
        } catch (NoSuchFieldException e) {
            // Embeddium's ChunkRenderTypeSet optimization is disabled
            return;
        }

        try {
            ChunkRenderTypeSet[] universe = (ChunkRenderTypeSet[]) universeField.get(null);
            int combinations = 1 << layerCount;
            if (universe.length == combinations) {
                return;
            }
            if (layerCount > EMBEDDIUM_MAX_LAYERS) {
                Veil.LOGGER.error("Embeddium supports at most {} chunk layers, but {} are registered. Chunk render type sets with custom block layers will not work", EMBEDDIUM_MAX_LAYERS, layerCount);
                return;
            }

            // Keep the existing sets so identity comparisons still hold. The last entry was the old "all" set
            ChunkRenderTypeSet[] newUniverse = Arrays.copyOf(universe, combinations);
            for (int i = Math.max(1, universe.length - 1); i < combinations - 1; i++) {
                newUniverse[i] = ChunkRenderTypeSetAccessor.veil$create(BitSet.valueOf(new long[]{i}));
            }
            newUniverse[0] = ChunkRenderTypeSet.none();
            newUniverse[combinations - 1] = all;

            maskField.setInt(all, combinations - 1);
            combinationsField.setInt(null, combinations);
            maskAllField.setInt(null, combinations - 1);
            universeField.set(null, newUniverse);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Veil.LOGGER.error("Failed to add custom block layers to Embeddium's chunk render type sets", e);
        }
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = ChunkRenderTypeSet.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
