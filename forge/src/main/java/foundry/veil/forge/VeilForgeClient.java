package foundry.veil.forge;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.forge.event.ForgeVeilRegisterFixedBuffersEvent;
import foundry.veil.forge.event.ForgeVeilRendererAvailableEvent;
import foundry.veil.forge.impl.ForgeRenderTypeStageHandler;
import foundry.veil.impl.VeilReloadListeners;
import foundry.veil.impl.client.imgui.VeilImGuiCompat;
import foundry.veil.impl.client.render.shader.VeilVanillaShaders;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VeilForgeClient {

    public static void init(IEventBus modEventBus) {
        VeilClient.init();

        modEventBus.addListener(VeilForgeClient::registerKeys);
        modEventBus.addListener(VeilForgeClient::registerListeners);
        modEventBus.addListener(VeilForgeClient::registerShaders);
    }

    private static void registerListeners(RegisterClientReloadListenersEvent event) {
        VeilRenderSystem.init();
        VeilReloadListeners.registerListeners((type, id, listener) -> event.registerReloadListener(listener));
        ModLoader.get().postEvent(new ForgeVeilRendererAvailableEvent(VeilRenderSystem.renderer()));
        ModLoader.get().postEvent(new ForgeVeilRegisterFixedBuffersEvent(ForgeRenderTypeStageHandler::register));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        if (Veil.IMGUIMC) {
            event.register(VeilImGuiCompat.EDITOR_KEY);
        }
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            VeilVanillaShaders.registerShaders((id, vertexFormat, loadCallback) -> event.registerShader(new ShaderInstance(event.getResourceProvider(), id, vertexFormat), loadCallback));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
