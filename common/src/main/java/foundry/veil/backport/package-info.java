/**
 * Minimal re-implementations of Minecraft 1.20.5+ utility types that Veil's public API is built on.
 * <p>
 * Forge 1.20.1 loads Minecraft as its own module, so these types can't live in their original {@code net.minecraft}
 * packages. Instead each type keeps its vanilla name and sits under {@code foundry.veil.backport} at the same relative
 * package, i.e. {@code net.minecraft.network.codec.StreamCodec} becomes {@code foundry.veil.backport.network.codec.StreamCodec}.
 * Code written against 1.21 can be ported by rewriting those imports.
 */
package foundry.veil.backport;
