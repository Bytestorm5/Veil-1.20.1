package foundry.veil.forge.platform;

import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import foundry.veil.platform.VeilPlatform;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ForgeVeilPlatform implements VeilPlatform {

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.FORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean canAttachRenderdoc() {
        if (!FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)) {
            return true;
        }

        String windowControl = FMLConfig.getConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_PROVIDER);
        return windowControl == null || "dummyprovider".equals(windowControl);
    }

    @Override
    public boolean hasErrors() {
        return !ModLoader.isLoadingStateValid();
    }

    @Override
    public boolean hasChannel(PacketListener listener, CustomPacketPayload.Type<?> type) {
        Connection connection;
        if (listener instanceof ServerGamePacketListenerImpl serverListener) {
            connection = serverListener.connection;
        } else {
            return false;
        }

        // Every Veil payload is sent on a channel named after the payload id
        ConnectionData data = NetworkHooks.getConnectionData(connection);
        return data != null && data.getChannels().containsKey(type.id());
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public @Nullable MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
