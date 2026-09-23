package foundry.veil.impl.network;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
import foundry.veil.Veil;
import io.netty.buffer.ByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundRemovePostProcessingPacket(ResourceLocation pipeline) implements CustomPacketPayload {

    public static final StreamCodec<ByteBuf, ClientboundRemovePostProcessingPacket> CODEC = VanillaStreamCodecs.RESOURCE_LOCATION
            .map(ClientboundRemovePostProcessingPacket::new, ClientboundRemovePostProcessingPacket::pipeline);
    public static final Type<ClientboundRemovePostProcessingPacket> TYPE = new Type<>(Veil.veilPath("remove_post_processing"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
