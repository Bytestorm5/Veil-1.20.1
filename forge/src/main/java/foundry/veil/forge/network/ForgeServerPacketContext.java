package foundry.veil.forge.network;

import foundry.veil.api.network.handler.ServerPacketContext;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import foundry.veil.impl.network.VeilPayloadRegistry;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public record ForgeServerPacketContext(NetworkEvent.Context context) implements ServerPacketContext {

    @Override
    public @NotNull ServerPlayer player() {
        return Objects.requireNonNull(this.context.getSender(), "sender");
    }

    @Override
    public Packet<?> createPacket(CustomPacketPayload payload) {
        return VeilPayloadRegistry.toClientbound(payload);
    }

    @Override
    public void sendPacket(Packet<?> packet, @Nullable PacketSendListener callback) {
        this.context.getNetworkManager().send(packet, callback);
    }

    @Override
    public void disconnect(Component disconnectReason) {
        this.player().connection.disconnect(disconnectReason);
    }
}
