package foundry.veil.mixin.pipeline.accessor;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(MultiBufferSource.BufferSource.class)
public interface PipelineBufferSourceAccessor {

    @Accessor
    Optional<RenderType> getLastState();
}
