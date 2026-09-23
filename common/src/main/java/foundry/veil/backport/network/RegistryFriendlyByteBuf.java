package foundry.veil.backport.network;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.ByteBuf;

import java.util.function.Function;

/**
 * Backport of {@code net.minecraft.network.RegistryFriendlyByteBuf}.
 */
public class RegistryFriendlyByteBuf extends FriendlyByteBuf {

    private final RegistryAccess registryAccess;

    public RegistryFriendlyByteBuf(ByteBuf source, RegistryAccess registryAccess) {
        super(source);
        this.registryAccess = registryAccess;
    }

    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }

    public static Function<ByteBuf, RegistryFriendlyByteBuf> decorator(RegistryAccess registryAccess) {
        return buf -> new RegistryFriendlyByteBuf(buf, registryAccess);
    }
}
