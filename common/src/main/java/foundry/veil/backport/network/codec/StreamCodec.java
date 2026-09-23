package foundry.veil.backport.network.codec;

import com.mojang.datafixers.util.*;
import io.netty.buffer.ByteBuf;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Backport of {@code net.minecraft.network.codec.StreamCodec}.
 *
 * @param <B> The buffer type
 * @param <V> The value type
 */
public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {

    static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(B buffer, V value) {
                encoder.encode(buffer, value);
            }
        };
    }

    static <B, V> StreamCodec<B, V> ofMember(StreamMemberEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(B buffer, V value) {
                encoder.encode(value, buffer);
            }
        };
    }

    static <B, V> StreamCodec<B, V> unit(V expected) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return expected;
            }

            @Override
            public void encode(B buffer, V value) {
                if (!value.equals(expected)) {
                    throw new IllegalStateException("Can't encode '" + value + "', expected '" + expected + "'");
                }
            }
        };
    }

    default <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> operation) {
        return operation.apply(this);
    }

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> factory, Function<? super O, ? extends V> getter) {
        return new StreamCodec<>() {
            @Override
            public O decode(B buffer) {
                return factory.apply(StreamCodec.this.decode(buffer));
            }

            @Override
            public void encode(B buffer, O value) {
                StreamCodec.this.encode(buffer, getter.apply(value));
            }
        };
    }

    default <O extends ByteBuf> StreamCodec<O, V> mapStream(Function<O, ? extends B> bufferFactory) {
        return new StreamCodec<>() {
            @Override
            public V decode(O buffer) {
                return StreamCodec.this.decode(bufferFactory.apply(buffer));
            }

            @Override
            public void encode(O buffer, V value) {
                StreamCodec.this.encode(bufferFactory.apply(buffer), value);
            }
        };
    }

    default <U> StreamCodec<B, U> dispatch(Function<? super U, ? extends V> keyGetter, Function<? super V, ? extends StreamCodec<? super B, ? extends U>> codecGetter) {
        return new StreamCodec<>() {
            @Override
            public U decode(B buffer) {
                V key = StreamCodec.this.decode(buffer);
                StreamCodec<? super B, ? extends U> codec = codecGetter.apply(key);
                return codec.decode(buffer);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void encode(B buffer, U value) {
                V key = keyGetter.apply(value);
                StreamCodec<B, U> codec = (StreamCodec<B, U>) codecGetter.apply(key);
                StreamCodec.this.encode(buffer, key);
                codec.encode(buffer, value);
            }
        };
    }

    static <B, C, T1> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            Function<T1, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                return factory.apply(t1);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            java.util.function.BiFunction<T1, T2, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                return factory.apply(t1, t2);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            Function3<T1, T2, T3, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                return factory.apply(t1, t2, t3);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            Function4<T1, T2, T3, T4, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                return factory.apply(t1, t2, t3, t4);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            Function5<T1, T2, T3, T4, T5, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            Function6<T1, T2, T3, T4, T5, T6, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
            }
        };
    }

    static <B, T> StreamCodec<B, T> recursive(UnaryOperator<StreamCodec<B, T>> modifier) {
        return new StreamCodec<>() {
            private StreamCodec<B, T> inner;

            private StreamCodec<B, T> inner() {
                if (this.inner == null) {
                    this.inner = modifier.apply(this);
                }
                return this.inner;
            }

            @Override
            public T decode(B buffer) {
                return this.inner().decode(buffer);
            }

            @Override
            public void encode(B buffer, T value) {
                this.inner().encode(buffer, value);
            }
        };
    }

    @SuppressWarnings("unchecked")
    default <S extends B> StreamCodec<S, V> cast() {
        return (StreamCodec<S, V>) this;
    }

    @FunctionalInterface
    interface CodecOperation<B, S, T> {

        StreamCodec<B, T> apply(StreamCodec<B, S> codec);
    }
}
