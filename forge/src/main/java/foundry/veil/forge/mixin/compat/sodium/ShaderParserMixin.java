package foundry.veil.forge.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.render.shader.processor.SodiumShaderProcessor;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderParser;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaderParser.class, remap = false)
public class ShaderParserMixin {

    @ModifyReturnValue(method = "parseShader(Ljava/lang/String;Lme/jellysquid/mods/sodium/client/gl/shader/ShaderConstants;)Ljava/lang/String;", at = @At("RETURN"))
    private static String modifySource(String original) {
        try {
            int activeBuffers = VeilRenderSystem.renderer().getDynamicBufferManger().getActiveBuffers();
            SodiumShaderProcessor.setup(Minecraft.getInstance().getResourceManager());
            return SodiumShaderProcessor.modify(activeBuffers, original);
        } catch (Throwable t) {
            Veil.LOGGER.error("Failed to apply Veil shader modifiers to shader: {}", SodiumShaderProcessor.getActiveShaderName(), t);
            return original;
        } finally {
            SodiumShaderProcessor.free();
        }
    }
}
