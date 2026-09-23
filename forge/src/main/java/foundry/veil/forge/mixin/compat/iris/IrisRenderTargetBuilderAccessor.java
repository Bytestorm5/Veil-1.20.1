package foundry.veil.forge.mixin.compat.iris;

import net.irisshaders.iris.targets.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RenderTarget.Builder.class, remap = false)
public interface IrisRenderTargetBuilderAccessor {

    @Accessor
    String getName();
}
