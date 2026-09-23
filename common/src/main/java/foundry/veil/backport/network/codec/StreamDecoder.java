package foundry.veil.backport.network.codec;

/**
 * Backport of {@code net.minecraft.network.codec.StreamDecoder}.
 */
@FunctionalInterface
public interface StreamDecoder<I, T> {

    T decode(I buffer);
}
