package foundry.veil.backport.network.protocol.common.custom;

import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.codec.StreamDecoder;
import foundry.veil.backport.network.codec.StreamMemberEncoder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Backport of {@code net.minecraft.network.protocol.common.custom.CustomPacketPayload}.
 * <p>
 * Payloads are sent through {@link foundry.veil.api.network.VeilPacketManager}, which encodes them into a vanilla
 * custom payload packet using the codec they were registered with.
 */
public interface CustomPacketPayload {

    Type<? extends CustomPacketPayload> type();

    static <B extends FriendlyByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> encoder, StreamDecoder<B, T> decoder) {
        return StreamCodec.ofMember(encoder, decoder);
    }

    static <T extends CustomPacketPayload> Type<T> createType(String id) {
        return new Type<>(new ResourceLocation(id));
    }

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }

    record TypeAndCodec<B extends FriendlyByteBuf, T extends CustomPacketPayload>(Type<T> type,
                                                                                   StreamCodec<B, T> codec) {
    }
}
