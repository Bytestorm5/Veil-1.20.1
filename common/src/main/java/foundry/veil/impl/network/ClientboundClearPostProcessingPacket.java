package foundry.veil.impl.network;

import foundry.veil.Veil;
import io.netty.buffer.ByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public enum ClientboundClearPostProcessingPacket implements CustomPacketPayload {

    INSTANCE;

    public static final StreamCodec<ByteBuf, ClientboundClearPostProcessingPacket> CODEC = StreamCodec.unit(INSTANCE);
    public static final Type<ClientboundClearPostProcessingPacket> TYPE = new Type<>(Veil.veilPath("clear_post_processing"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
