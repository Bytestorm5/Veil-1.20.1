package foundry.veil.impl.network;

import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum ClientPacketSink implements VeilPacketManager.PacketSink {

    INSTANCE;

    @Override
    public void sendPacket(CustomPacketPayload... payloads) {
        for (CustomPacketPayload payload : payloads) {
            this.sendPacket(VeilPayloadRegistry.toServerbound(payload));
        }
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Cannot send client packets while not connected to a server");
        }

        connection.send(packet);
    }
}
