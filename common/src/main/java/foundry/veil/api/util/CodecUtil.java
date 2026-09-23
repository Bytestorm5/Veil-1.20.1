package foundry.veil.api.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.Veil;
import io.github.ocelot.glslprocessor.api.grammar.GlslTypeSpecifier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.joml.*;

import java.util.*;
import java.util.function.Function;

public class CodecUtil {

    public static final EnumCodec<GlslTypeSpecifier.BuiltinType> BUILTIN_TYPE_CODEC = EnumCodec.<GlslTypeSpecifier.BuiltinType>builder("glsl_type").values(GlslTypeSpecifier.BuiltinType.values()).build();

    public static final Codec<Vector2fc> VECTOR2FC_CODEC = Codec.FLOAT.listOf()
            .flatXmap(list -> check(3, list), list -> check(2, list))
            .xmap(list -> new Vector2f(list.get(0), list.get(1)),
                    vector -> List.of(vector.x(), vector.y()));

    public static final Codec<Vector3fc> VECTOR3FC_CODEC = Codec.FLOAT.listOf()
            .flatXmap(list -> check(3, list), list -> check(3, list))
            .xmap(list -> new Vector3f(list.get(0), list.get(1), list.get(2)),
                    vector -> List.of(vector.x(), vector.y(), vector.z()));

    public static final Codec<Vector4fc> VECTOR4FC_CODEC = Codec.FLOAT.listOf()
            .flatXmap(list -> check(4, list), list -> check(4, list))
            .xmap(list -> new Vector4f(list.get(0), list.get(1), list.get(2), list.get(3)),
                    vector -> List.of(vector.x(), vector.y(), vector.z(), vector.w()));

    public static final Codec<Vector2dc> VECTOR2DC_CODEC = Codec.DOUBLE.listOf()
            .flatXmap(list -> check(3, list), list -> check(2, list))
            .xmap(list -> new Vector2d(list.get(0), list.get(1)),
                    vector -> List.of(vector.x(), vector.y()));

    public static final Codec<Vector3dc> VECTOR3DC_CODEC = Codec.DOUBLE.listOf()
            .flatXmap(list -> check(3, list), list -> check(3, list))
            .xmap(list -> new Vector3d(list.get(0), list.get(1), list.get(2)),
                    vector -> List.of(vector.x(), vector.y(), vector.z()));

    public static final Codec<Vector4dc> VECTOR4DC_CODEC = Codec.DOUBLE.listOf()
            .flatXmap(list -> check(4, list), list -> check(4, list))
            .xmap(list -> new Vector4d(list.get(0), list.get(1), list.get(2), list.get(3)),
                    vector -> List.of(vector.x(), vector.y(), vector.z(), vector.w()));

    public static final Codec<Matrix3fc> MATRIX3FC_CODEC = CodecUtil.listOf(VECTOR3FC_CODEC, 3, 3)
            .xmap(list -> new Matrix3f(list.get(0), list.get(1), list.get(2)),
                    matrix -> List.of(matrix.getColumn(0, new Vector3f()),
                            matrix.getColumn(1, new Vector3f()),
                            matrix.getColumn(2, new Vector3f())));

    public static final Codec<Matrix4fc> MATRIX4FC_CODEC = CodecUtil.listOf(VECTOR4FC_CODEC, 3, 3)
            .xmap(list -> new Matrix4f(list.get(0), list.get(1), list.get(2), list.get(3)),
                    matrix -> List.of(matrix.getColumn(0, new Vector4f()),
                            matrix.getColumn(1, new Vector4f()),
                            matrix.getColumn(2, new Vector4f()),
                            matrix.getColumn(3, new Vector4f())));

    public static final Codec<Matrix3dc> MATRIX3DC_CODEC = CodecUtil.listOf(VECTOR3DC_CODEC, 3, 3)
            .xmap(list -> new Matrix3d(list.get(0), list.get(1), list.get(2)),
                    matrix -> List.of(matrix.getColumn(0, new Vector3d()),
                            matrix.getColumn(1, new Vector3d()),
                            matrix.getColumn(2, new Vector3d())));

    public static final Codec<Matrix4dc> MATRIX4DC_CODEC = CodecUtil.listOf(VECTOR4DC_CODEC, 3, 3)
            .xmap(list -> new Matrix4d((Vector4d) list.get(0), (Vector4d) list.get(1), (Vector4d) list.get(2), (Vector4d) list.get(3)),
                    matrix -> List.of(matrix.getColumn(0, new Vector4d()),
                            matrix.getColumn(1, new Vector4d()),
                            matrix.getColumn(2, new Vector4d()),
                            matrix.getColumn(3, new Vector4d())));

    public static final Codec<Vector2f> VECTOR2F_CODEC = Codec.FLOAT.listOf()
            .flatXmap(list -> check(3, list), list -> check(2, list))
            .xmap(list -> new Vector2f(list.get(0), list.get(1)),
                    vector -> List.of(vector.x(), vector.y()));

    public static final Codec<Vector3f> VECTOR3F_CODEC = Codec.FLOAT.listOf()
            .flatXmap(list -> check(3, list), list -> check(3, list))
            .xmap(list -> new Vector3f(list.get(0), list.get(1), list.get(2)),
                    vector -> List.of(vector.x(), vector.y(), vector.z()));

    public static final Codec<Vector4f> VECTOR4F_CODEC = Codec.FLOAT.listOf()
            .flatXmap(list -> check(4, list), list -> check(4, list))
            .xmap(list -> new Vector4f(list.get(0), list.get(1), list.get(2), list.get(3)),
                    vector -> List.of(vector.x(), vector.y(), vector.z(), vector.w()));

    public static final Codec<Vector2d> VECTOR2D_CODEC = Codec.DOUBLE.listOf()
            .flatXmap(list -> check(3, list), list -> check(2, list))
            .xmap(list -> new Vector2d(list.get(0), list.get(1)),
                    vector -> List.of(vector.x(), vector.y()));

    public static final Codec<Vector3d> VECTOR3D_CODEC = Codec.DOUBLE.listOf()
            .flatXmap(list -> check(3, list), list -> check(3, list))
            .xmap(list -> new Vector3d(list.get(0), list.get(1), list.get(2)),
                    vector -> List.of(vector.x(), vector.y(), vector.z()));

    public static final Codec<Vector4d> VECTOR4D_CODEC = Codec.DOUBLE.listOf()
            .flatXmap(list -> check(4, list), list -> check(4, list))
            .xmap(list -> new Vector4d(list.get(0), list.get(1), list.get(2), list.get(3)),
                    vector -> List.of(vector.x(), vector.y(), vector.z(), vector.w()));

    public static final Codec<Matrix3f> MATRIX3F_CODEC = CodecUtil.listOf(VECTOR3FC_CODEC, 3, 3)
            .xmap(list -> new Matrix3f(list.get(0), list.get(1), list.get(2)),
                    matrix -> List.of(matrix.getColumn(0, new Vector3f()),
                            matrix.getColumn(1, new Vector3f()),
                            matrix.getColumn(2, new Vector3f())));

    public static final Codec<Matrix4f> MATRIX4F_CODEC = CodecUtil.listOf(VECTOR4FC_CODEC, 3, 3)
            .xmap(list -> new Matrix4f(list.get(0), list.get(1), list.get(2), list.get(3)),
                    matrix -> List.of(matrix.getColumn(0, new Vector4f()),
                            matrix.getColumn(1, new Vector4f()),
                            matrix.getColumn(2, new Vector4f()),
                            matrix.getColumn(3, new Vector4f())));

    public static final Codec<Matrix3dc> MATRIX3D_CODEC = CodecUtil.listOf(VECTOR3DC_CODEC, 3, 3)
            .xmap(list -> new Matrix3d(list.get(0), list.get(1), list.get(2)),
                    matrix -> List.of(matrix.getColumn(0, new Vector3d()),
                            matrix.getColumn(1, new Vector3d()),
                            matrix.getColumn(2, new Vector3d())));

    public static final Codec<Matrix4d> MATRIX4D_CODEC = CodecUtil.listOf(VECTOR4DC_CODEC, 3, 3)
            .xmap(list -> new Matrix4d((Vector4d) list.get(0), (Vector4d) list.get(1), (Vector4d) list.get(2), (Vector4d) list.get(3)),
                    matrix -> List.of(matrix.getColumn(0, new Vector4d()),
                            matrix.getColumn(1, new Vector4d()),
                            matrix.getColumn(2, new Vector4d()),
                            matrix.getColumn(3, new Vector4d())));

    private static <T> DataResult<List<T>> check(int size, List<T> list) {
        if (list.size() != size) {
            return DataResult.error(() -> "Vector" + size + "f must have " + size + " elements!");
        }
        return DataResult.success(list);
    }

    public static <T> Codec<List<T>> singleOrList(Codec<T> codec) {
        return Codec.either(
                        codec.flatComapMap(List::of,
                                l -> l.size() == 1
                                        ? DataResult.success(l.get(0))
                                        : DataResult.error(() -> "List must have exactly one element.")),
                        ExtraCodecs.nonEmptyList(codec.listOf()))
                .xmap(e -> e.map(Function.identity(), Function.identity()),
                        l -> l.size() == 1 ? Either.left(l) : Either.right(l));
    }

    /**
     * Creates a codec which can accept either resource locations like `veil:cube`
     * but also accepts legacy-style names like `CUBE` (used when things used to be
     * enums, but are now registries)
     */
    public static <T> Codec<T> registryOrLegacyCodec(Registry<T> registry) {
        Codec<T> legacyCodec = Codec.STRING
                .comapFlatMap(
                        name -> ResourceLocation.read(Veil.MODID + ":" + name.toLowerCase(Locale.ROOT)),
                        ResourceLocation::toString)
                .flatXmap(
                        loc -> Optional.ofNullable(registry.get(loc))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + registry.key() + ": " + loc)),
                        object -> registry.getResourceKey(object)
                                .map(ResourceKey::location)
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + registry.key() + ":" + object)));

        return Codec.either(registry.byNameCodec(), legacyCodec)
                .xmap(e -> e.map(Function.identity(), Function.identity()), Either::left);
    }

    public static <V, K> Map<V, K> pairListToMap(List<? extends Pair<? extends V, ? extends K>> pairList) {
        Map<V, K> map = new HashMap<>();
        for (Pair<? extends V, ? extends K> pair : pairList) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return map;
    }

    public static <V, K> List<Pair<V, K>> entrySetToPairList(Set<? extends Map.Entry<? extends V, ? extends K>> entrySet) {
        return entrySet.stream().map(entry -> new Pair<V, K>(entry.getKey(), entry.getValue())).toList();
    }

    /**
     * Equivalent of DFU 8's {@code Codec#listOf(int, int)}.
     *
     * @param codec   The element codec
     * @param minSize The minimum number of elements, inclusive
     * @param maxSize The maximum number of elements, inclusive
     * @return A list codec that validates the list size
     */
    public static <T> Codec<List<T>> listOf(Codec<T> codec, int minSize, int maxSize) {
        Function<List<T>, DataResult<List<T>>> checker = list -> {
            int size = list.size();
            if (size < minSize) {
                return DataResult.error(() -> "List is too short: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            if (size > maxSize) {
                return DataResult.error(() -> "List is too long: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            return DataResult.success(list);
        };
        return codec.listOf().flatXmap(checker, checker);
    }

    /**
     * Equivalent of DFU 8's {@code Codec#withAlternative(Codec, Codec)}.
     */
    public static <T> Codec<T> withAlternative(Codec<T> primary, Codec<? extends T> alternative) {
        return Codec.either(primary, alternative).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
    }

    /**
     * Equivalent of DFU 8's {@code DataResult#getOrThrow()}.
     *
     * @throws IllegalStateException If the result is an error
     */
    public static <T> T getOrThrow(DataResult<T> result) {
        return result.getOrThrow(false, error -> {
        });
    }

    /**
     * Equivalent of DFU 8's {@code DataResult#isSuccess()}.
     */
    public static boolean isSuccess(DataResult<?> result) {
        return result.result().isPresent();
    }

    /**
     * Equivalent of DFU 8's {@code DataResult#isError()}.
     */
    public static boolean isError(DataResult<?> result) {
        return result.error().isPresent();
    }
}
