package foundry.veil.forge.network;

import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import foundry.veil.impl.network.VeilPayloadRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public record ForgeClientPacketContext(NetworkEvent.Context context) implements ClientPacketContext {

    @Override
    public Minecraft client() {
        return Minecraft.getInstance();
    }

    @Override
    public LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload payload) {
        return VeilPayloadRegistry.toServerbound(payload);
    }

    @Override
    public void sendPacket(Packet<?> packet, @Nullable PacketSendListener callback) {
        this.context.getNetworkManager().send(packet, callback);
    }

    @Override
    public void disconnect(Component disconnectReason) {
        this.context.getNetworkManager().disconnect(disconnectReason);
    }
}
