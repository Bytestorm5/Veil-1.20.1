package foundry.veil.forge.platform;

import foundry.veil.api.client.render.dynamicbuffer.DynamicBuffersChange;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilAddShaderPreProcessorsEvent;
import foundry.veil.api.event.VeilRegisterGlobalControllersEvent;
import foundry.veil.api.event.VeilRegisterInspectorsEvent;
import foundry.veil.forge.event.*;
import foundry.veil.platform.VeilClientPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Internal
public class ForgeVeilClientPlatform implements VeilClientPlatform {

    @Override
    public void preVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        MinecraftForge.EVENT_BUS.post(new ForgeVeilPostProcessingEvent.Pre(name, pipeline, context));
    }

    @Override
    public void postVeilPostProcessing(ResourceLocation name, PostPipeline pipeline, PostPipeline.Context context) {
        MinecraftForge.EVENT_BUS.post(new ForgeVeilPostProcessingEvent.Post(name, pipeline, context));
    }

    @Override
    public void onRegisterShaderPreProcessors(ResourceProvider resourceProvider, VeilAddShaderPreProcessorsEvent.Registry registry) {
        ModLoader.get().postEvent(new ForgeVeilAddShaderProcessorsEvent(resourceProvider, registry));
    }

    @Override
    public void onRegisterGlobalControllers(VeilRegisterGlobalControllersEvent.Registry registry) {
        ModLoader.get().postEvent(new ForgeVeilRegisterGlobalControllersEvent(registry));
    }

    @Override
    public void onRegisterInspectors(VeilRegisterInspectorsEvent.Registry registry) {
        ModLoader.get().postEvent(new ForgeVeilRegisterInspectorsEvent(registry));
    }

    @Override
    public void onVeilCompileShaders(ShaderManager shaderManager, Map<ResourceLocation, ShaderProgram> updatedPrograms) {
        ModLoader.get().postEvent(new ForgeVeilShaderCompileEvent(shaderManager, updatedPrograms));
    }

    @Override
    public void onVeilDynamicBuffersChanged(DynamicBuffersChange change) {
        ModLoader.get().postEvent(new ForgeVeilDynamicBuffersChangedEvent(change));
    }
}
