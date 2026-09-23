package foundry.veil.backport.network.codec;

/**
 * Backport of {@code net.minecraft.network.codec.StreamMemberEncoder}.
 */
@FunctionalInterface
public interface StreamMemberEncoder<O, T> {

    void encode(T value, O buffer);
}
