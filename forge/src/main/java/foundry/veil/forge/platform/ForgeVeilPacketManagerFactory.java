package foundry.veil.forge.platform;

import foundry.veil.api.network.VeilPacketManager;
import foundry.veil.forge.network.ForgeVeilPacketManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ForgeVeilPacketManagerFactory implements VeilPacketManager.Factory {

    @Override
    public VeilPacketManager create(String modId, String version) {
        return new ForgeVeilPacketManager(modId, version);
    }
}
