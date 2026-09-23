package foundry.veil.forge.mixin.compat.sodium;

import foundry.veil.impl.client.render.shader.processor.SodiumShaderProcessor;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaderLoader.class, remap = false)
public class ShaderLoaderMixin {

    @Inject(method = "loadShader", at = @At("HEAD"))
    private static void preLoadShader(ShaderType type, ResourceLocation name, ShaderConstants constants, CallbackInfoReturnable<GlShader> cir) {
        SodiumShaderProcessor.setShaderType(type.id, name, GL.getCapabilities());
    }
}
