package foundry.veil.backport.network.codec;

/**
 * Backport of {@code net.minecraft.network.codec.StreamEncoder}.
 */
@FunctionalInterface
public interface StreamEncoder<O, T> {

    void encode(O buffer, T value);
}
