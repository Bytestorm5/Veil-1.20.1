package foundry.veil.forge.network;

import foundry.veil.Veil;
import foundry.veil.api.network.VeilPacketManager;
import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.network.handler.PacketContext;
import foundry.veil.api.network.handler.ServerPacketContext;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import foundry.veil.impl.network.VeilPayloadRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Forge 1.20.1 has no payload registrar, so every payload type gets its own {@link EventNetworkChannel} named after the
 * payload id. Payloads are encoded into vanilla custom payload packets by {@link VeilPayloadRegistry}.
 */
@ApiStatus.Internal
public class ForgeVeilPacketManager implements VeilPacketManager {

    private final String modId;
    private final String version;

    public ForgeVeilPacketManager(String modId, String version) {
        this.modId = modId;
        this.version = version;
    }

    private EventNetworkChannel createChannel(ResourceLocation id, boolean optional) {
        Predicate<String> accepted = optional ? NetworkRegistry.acceptMissingOr(this.version) : this.version::equals;
        return NetworkRegistry.ChannelBuilder.named(id)
                .networkProtocolVersion(() -> this.version)
                .clientAcceptedVersions(accepted)
                .serverAcceptedVersions(accepted)
                .eventNetworkChannel();
    }

    private <T extends CustomPacketPayload, C extends PacketContext> void register(CustomPacketPayload.Type<T> id,
                                                                                  NetworkDirection direction,
                                                                                  Function<NetworkEvent.Context, C> contextFactory,
                                                                                  PacketHandler<C, T> handler,
                                                                                  boolean optional) {
        EventNetworkChannel channel = this.createChannel(id.id(), optional);
        channel.addListener((NetworkEvent event) -> {
            FriendlyByteBuf payloadBuffer = event.getPayload();
            NetworkEvent.Context context = event.getSource().get();
            if (payloadBuffer == null || context.getDirection() != direction) {
                return;
            }

            CustomPacketPayload decoded = direction == NetworkDirection.PLAY_TO_CLIENT ?
                    VeilPayloadRegistry.decodeClientbound(id.id(), payloadBuffer) :
                    VeilPayloadRegistry.decodeServerbound(id.id(), payloadBuffer);
            @SuppressWarnings("unchecked")
            T payload = (T) decoded;
            context.enqueueWork(() -> {
                try {
                    handler.handlePacket(payload, contextFactory.apply(context));
                } catch (Throwable t) {
                    Veil.LOGGER.error("Failed to handle packet {} from mod {}", id.id(), this.modId, t);
                }
            });
            context.setPacketHandled(true);
        });
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientbound(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<ClientPacketContext, T> handler, boolean optional) {
        VeilPayloadRegistry.registerClientbound(id, codec);
        this.register(id, NetworkDirection.PLAY_TO_CLIENT, ForgeClientPacketContext::new, handler, optional);
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerbound(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<ServerPacketContext, T> handler, boolean optional) {
        VeilPayloadRegistry.registerServerbound(id, codec);
        this.register(id, NetworkDirection.PLAY_TO_SERVER, ForgeServerPacketContext::new, handler, optional);
    }
}
