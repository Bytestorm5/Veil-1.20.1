package foundry.veil;

import foundry.veil.impl.VeilMixinPlugin;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Set;

public class VeilMixinPluginImpl extends VeilMixinPlugin {

    private static final String CHUNK_RENDER_TYPE_SET_ACCESSOR = "foundry.veil.forge.mixin.client.ChunkRenderTypeSetAccessor";
    // Fields Embeddium's ChunkRenderTypeSetMixin adds, which ForgeChunkRenderTypeSetSync resizes for custom block layers
    private static final Set<String> EMBEDDIUM_CHUNK_RENDER_TYPE_SET_FIELDS = Set.of("UNIVERSE", "MASK_ALL", "POSSIBLE_RENDER_TYPE_COMBINATIONS", "mask");

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (CHUNK_RENDER_TYPE_SET_ACCESSOR.equals(mixinClassName)) {
            for (FieldNode field : targetClass.fields) {
                if (EMBEDDIUM_CHUNK_RENDER_TYPE_SET_FIELDS.contains(field.name)) {
                    field.access &= ~Opcodes.ACC_FINAL;
                }
            }
        }
    }
}
