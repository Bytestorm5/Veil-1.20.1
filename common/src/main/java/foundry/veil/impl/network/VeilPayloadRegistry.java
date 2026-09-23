package foundry.veil.impl.network;

import foundry.veil.Veil;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the codec of every payload registered through {@link foundry.veil.api.network.VeilPacketManager} so payloads
 * can be turned into vanilla 1.20.1 custom payload packets, which only carry an id and raw bytes.
 */
@ApiStatus.Internal
public final class VeilPayloadRegistry {

    private static final Map<ResourceLocation, StreamCodec<? super RegistryFriendlyByteBuf, ?>> CLIENTBOUND = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, StreamCodec<? super RegistryFriendlyByteBuf, ?>> SERVERBOUND = new ConcurrentHashMap<>();

    private VeilPayloadRegistry() {
    }

    public static void registerClientbound(CustomPacketPayload.Type<?> type, StreamCodec<? super RegistryFriendlyByteBuf, ?> codec) {
        if (CLIENTBOUND.putIfAbsent(type.id(), codec) != null) {
            throw new IllegalStateException("Duplicate clientbound payload: " + type.id());
        }
    }

    public static void registerServerbound(CustomPacketPayload.Type<?> type, StreamCodec<? super RegistryFriendlyByteBuf, ?> codec) {
        if (SERVERBOUND.putIfAbsent(type.id(), codec) != null) {
            throw new IllegalStateException("Duplicate serverbound payload: " + type.id());
        }
    }

    /**
     * Encodes a payload into a packet sent from the server to clients.
     */
    public static ClientboundCustomPayloadPacket toClientbound(CustomPacketPayload payload) {
        return new ClientboundCustomPayloadPacket(payload.type().id(), encode(CLIENTBOUND, payload, serverRegistryAccess()));
    }

    /**
     * Encodes a payload into a packet sent from the client to the server.
     */
    public static ServerboundCustomPayloadPacket toServerbound(CustomPacketPayload payload) {
        return new ServerboundCustomPayloadPacket(payload.type().id(), encode(SERVERBOUND, payload, clientRegistryAccess()));
    }

    /**
     * Decodes a received clientbound payload.
     */
    public static CustomPacketPayload decodeClientbound(ResourceLocation id, FriendlyByteBuf buf) {
        return decode(CLIENTBOUND, id, buf, clientRegistryAccess());
    }

    /**
     * Decodes a received serverbound payload.
     */
    public static CustomPacketPayload decodeServerbound(ResourceLocation id, FriendlyByteBuf buf) {
        return decode(SERVERBOUND, id, buf, serverRegistryAccess());
    }

    @SuppressWarnings("unchecked")
    private static FriendlyByteBuf encode(Map<ResourceLocation, StreamCodec<? super RegistryFriendlyByteBuf, ?>> codecs, CustomPacketPayload payload, RegistryAccess registryAccess) {
        ResourceLocation id = payload.type().id();
        StreamCodec<? super RegistryFriendlyByteBuf, CustomPacketPayload> codec = (StreamCodec<? super RegistryFriendlyByteBuf, CustomPacketPayload>) codecs.get(id);
        if (codec == null) {
            throw new IllegalArgumentException("Unregistered payload: " + id);
        }
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        codec.encode(buf, payload);
        return buf;
    }

    private static CustomPacketPayload decode(Map<ResourceLocation, StreamCodec<? super RegistryFriendlyByteBuf, ?>> codecs, ResourceLocation id, FriendlyByteBuf buf, RegistryAccess registryAccess) {
        StreamCodec<? super RegistryFriendlyByteBuf, ?> codec = codecs.get(id);
        if (codec == null) {
            throw new IllegalArgumentException("Unregistered payload: " + id);
        }
        Object value = codec.decode(new RegistryFriendlyByteBuf(buf, registryAccess));
        if (!(value instanceof CustomPacketPayload payload)) {
            throw new IllegalStateException("Codec for " + id + " did not produce a payload: " + value);
        }
        return payload;
    }

    private static RegistryAccess serverRegistryAccess() {
        MinecraftServer server = Veil.platform().getServer();
        if (server != null) {
            return server.registryAccess();
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    private static RegistryAccess clientRegistryAccess() {
        if (Veil.platform().isClient()) {
            RegistryAccess access = ClientAccess.get();
            if (access != null) {
                return access;
            }
        }
        return serverRegistryAccess();
    }

    private static final class ClientAccess {

        private static @Nullable RegistryAccess get() {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            return connection != null ? connection.registryAccess() : null;
        }
    }
}
