package foundry.veil.backport.network.codec;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Backport of {@code net.minecraft.network.codec.ByteBufCodecs}.
 * <p>
 * The wire format of every primitive codec matches the 1.21 one. Tag codecs wrap arbitrary tags in a compound, since
 * 1.20.1's {@link FriendlyByteBuf} can only read compound tags.
 */
public interface ByteBufCodecs {

    int MAX_INITIAL_COLLECTION_SIZE = 65536;

    StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<>() {
        @Override
        public Boolean decode(ByteBuf buffer) {
            return buffer.readBoolean();
        }

        @Override
        public void encode(ByteBuf buffer, Boolean value) {
            buffer.writeBoolean(value);
        }
    };
    StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<>() {
        @Override
        public Byte decode(ByteBuf buffer) {
            return buffer.readByte();
        }

        @Override
        public void encode(ByteBuf buffer, Byte value) {
            buffer.writeByte(value);
        }
    };
    StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<>() {
        @Override
        public Short decode(ByteBuf buffer) {
            return buffer.readShort();
        }

        @Override
        public void encode(ByteBuf buffer, Short value) {
            buffer.writeShort(value);
        }
    };
    StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<>() {
        @Override
        public Integer decode(ByteBuf buffer) {
            return buffer.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf buffer, Integer value) {
            buffer.writeShort(value);
        }
    };
    StreamCodec<ByteBuf, Integer> INT = new StreamCodec<>() {
        @Override
        public Integer decode(ByteBuf buffer) {
            return buffer.readInt();
        }

        @Override
        public void encode(ByteBuf buffer, Integer value) {
            buffer.writeInt(value);
        }
    };
    StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<>() {
        @Override
        public Integer decode(ByteBuf buffer) {
            return wrap(buffer).readVarInt();
        }

        @Override
        public void encode(ByteBuf buffer, Integer value) {
            wrap(buffer).writeVarInt(value);
        }
    };
    StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<>() {
        @Override
        public Long decode(ByteBuf buffer) {
            return wrap(buffer).readVarLong();
        }

        @Override
        public void encode(ByteBuf buffer, Long value) {
            wrap(buffer).writeVarLong(value);
        }
    };
    StreamCodec<ByteBuf, Long> LONG = new StreamCodec<>() {
        @Override
        public Long decode(ByteBuf buffer) {
            return buffer.readLong();
        }

        @Override
        public void encode(ByteBuf buffer, Long value) {
            buffer.writeLong(value);
        }
    };
    StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<>() {
        @Override
        public Float decode(ByteBuf buffer) {
            return buffer.readFloat();
        }

        @Override
        public void encode(ByteBuf buffer, Float value) {
            buffer.writeFloat(value);
        }
    };
    StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<>() {
        @Override
        public Double decode(ByteBuf buffer) {
            return buffer.readDouble();
        }

        @Override
        public void encode(ByteBuf buffer, Double value) {
            buffer.writeDouble(value);
        }
    };
    StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<>() {
        @Override
        public byte[] decode(ByteBuf buffer) {
            return wrap(buffer).readByteArray();
        }

        @Override
        public void encode(ByteBuf buffer, byte[] value) {
            wrap(buffer).writeByteArray(value);
        }
    };
    StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    StreamCodec<ByteBuf, Tag> TAG = tagCodec(() -> new NbtAccounter(2097152L));
    StreamCodec<ByteBuf, Tag> TRUSTED_TAG = tagCodec(() -> NbtAccounter.UNLIMITED);
    StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = compoundTagCodec(() -> new NbtAccounter(2097152L));
    StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = compoundTagCodec(() -> NbtAccounter.UNLIMITED);
    StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<>() {
        @Override
        public Optional<CompoundTag> decode(ByteBuf buffer) {
            return Optional.ofNullable(wrap(buffer).readNbt());
        }

        @Override
        public void encode(ByteBuf buffer, Optional<CompoundTag> value) {
            wrap(buffer).writeNbt(value.orElse(null));
        }
    };
    StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<>() {
        @Override
        public Vector3f decode(ByteBuf buffer) {
            return wrap(buffer).readVector3f();
        }

        @Override
        public void encode(ByteBuf buffer, Vector3f value) {
            wrap(buffer).writeVector3f(value);
        }
    };
    StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<>() {
        @Override
        public Quaternionf decode(ByteBuf buffer) {
            return wrap(buffer).readQuaternion();
        }

        @Override
        public void encode(ByteBuf buffer, Quaternionf value) {
            wrap(buffer).writeQuaternion(value);
        }
    };
    StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<>() {
        @Override
        public PropertyMap decode(ByteBuf buffer) {
            return wrap(buffer).readGameProfileProperties();
        }

        @Override
        public void encode(ByteBuf buffer, PropertyMap value) {
            wrap(buffer).writeGameProfileProperties(value);
        }
    };
    StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<>() {
        @Override
        public GameProfile decode(ByteBuf buffer) {
            return wrap(buffer).readGameProfile();
        }

        @Override
        public void encode(ByteBuf buffer, GameProfile value) {
            wrap(buffer).writeGameProfile(value);
        }
    };

    /**
     * Views the specified buffer as a {@link FriendlyByteBuf}, without copying.
     *
     * @param buffer The buffer to wrap
     * @return The buffer as a friendly byte buf
     */
    static FriendlyByteBuf wrap(ByteBuf buffer) {
        return buffer instanceof FriendlyByteBuf friendlyByteBuf ? friendlyByteBuf : new FriendlyByteBuf(buffer);
    }

    static StreamCodec<ByteBuf, byte[]> byteArray(int maxSize) {
        return new StreamCodec<>() {
            @Override
            public byte[] decode(ByteBuf buffer) {
                return wrap(buffer).readByteArray(maxSize);
            }

            @Override
            public void encode(ByteBuf buffer, byte[] value) {
                if (value.length > maxSize) {
                    throw new EncoderException("ByteArray with size " + value.length + " is bigger than allowed " + maxSize);
                }
                wrap(buffer).writeByteArray(value);
            }
        };
    }

    static StreamCodec<ByteBuf, String> stringUtf8(int maxLength) {
        return new StreamCodec<>() {
            @Override
            public String decode(ByteBuf buffer) {
                return wrap(buffer).readUtf(maxLength);
            }

            @Override
            public void encode(ByteBuf buffer, String value) {
                wrap(buffer).writeUtf(value, maxLength);
            }
        };
    }

    static StreamCodec<ByteBuf, Tag> tagCodec(Supplier<NbtAccounter> accounter) {
        return new StreamCodec<>() {
            @Override
            public Tag decode(ByteBuf buffer) {
                CompoundTag wrapper = wrap(buffer).readNbt(accounter.get());
                if (wrapper == null) {
                    throw new DecoderException("Expected non-null tag");
                }
                Tag tag = wrapper.get("value");
                if (tag == null) {
                    throw new DecoderException("Expected wrapped tag");
                }
                return tag;
            }

            @Override
            public void encode(ByteBuf buffer, Tag value) {
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("value", value);
                wrap(buffer).writeNbt(wrapper);
            }
        };
    }

    static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> accounter) {
        return new StreamCodec<>() {
            @Override
            public CompoundTag decode(ByteBuf buffer) {
                CompoundTag tag = wrap(buffer).readNbt(accounter.get());
                if (tag == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return tag;
            }

            @Override
            public void encode(ByteBuf buffer, CompoundTag value) {
                if (value == null) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                wrap(buffer).writeNbt(value);
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> codec) {
        return fromCodec(codec, () -> NbtAccounter.UNLIMITED);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return fromCodec(codec, () -> new NbtAccounter(2097152L));
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec, Supplier<NbtAccounter> accounter) {
        return tagCodec(accounter).map(tag -> decodeTag(NbtOps.INSTANCE, codec, tag), value -> encodeTag(NbtOps.INSTANCE, codec, value));
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> codec) {
        return fromCodecWithRegistries(codec, () -> NbtAccounter.UNLIMITED);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
        return fromCodecWithRegistries(codec, () -> new NbtAccounter(2097152L));
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec, Supplier<NbtAccounter> accounter) {
        StreamCodec<ByteBuf, Tag> tagCodec = tagCodec(accounter);
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf buffer) {
                Tag tag = tagCodec.decode(buffer);
                return decodeTag(RegistryOps.create(NbtOps.INSTANCE, buffer.registryAccess()), codec, tag);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, T value) {
                tagCodec.encode(buffer, encodeTag(RegistryOps.create(NbtOps.INSTANCE, buffer.registryAccess()), codec, value));
            }
        };
    }

    private static <T> T decodeTag(DynamicOps<Tag> ops, Codec<T> codec, Tag tag) {
        return Util.getOrThrow(codec.parse(ops, tag), error -> new DecoderException("Failed to decode: " + error + " " + tag));
    }

    private static <T> Tag encodeTag(DynamicOps<Tag> ops, Codec<T> codec, T value) {
        return Util.getOrThrow(codec.encodeStart(ops, value), error -> new EncoderException("Failed to encode: " + error + " " + value));
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(StreamCodec<B, V> codec) {
        return new StreamCodec<>() {
            @Override
            public Optional<V> decode(B buffer) {
                return buffer.readBoolean() ? Optional.of(codec.decode(buffer)) : Optional.empty();
            }

            @Override
            public void encode(B buffer, Optional<V> value) {
                if (value.isPresent()) {
                    buffer.writeBoolean(true);
                    codec.encode(buffer, value.get());
                } else {
                    buffer.writeBoolean(false);
                }
            }
        };
    }

    static int readCount(ByteBuf buffer, int maxSize) {
        int count = VAR_INT.decode(buffer);
        if (count > maxSize) {
            throw new DecoderException(count + " elements exceeded max size of: " + maxSize);
        }
        return count;
    }

    static void writeCount(ByteBuf buffer, int count, int maxSize) {
        if (count > maxSize) {
            throw new EncoderException(count + " elements exceeded max size of: " + maxSize);
        }
        VAR_INT.encode(buffer, count);
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> factory, StreamCodec<? super B, V> codec) {
        return collection(factory, codec, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> factory, StreamCodec<? super B, V> codec, int maxSize) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                int count = readCount(buffer, maxSize);
                C collection = factory.apply(Math.min(count, MAX_INITIAL_COLLECTION_SIZE));
                for (int i = 0; i < count; i++) {
                    collection.add(codec.decode(buffer));
                }
                return collection;
            }

            @Override
            public void encode(B buffer, C value) {
                writeCount(buffer, value.size(), maxSize);
                for (V element : value) {
                    codec.encode(buffer, element);
                }
            }
        };
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> factory) {
        return codec -> collection(factory, codec);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return codec -> collection(ArrayList::new, codec);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int maxSize) {
        return codec -> collection(ArrayList::new, codec, maxSize);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> factory, StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec) {
        return map(factory, keyCodec, valueCodec, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> factory, StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec, int maxSize) {
        return new StreamCodec<>() {
            @Override
            public M decode(B buffer) {
                int count = readCount(buffer, maxSize);
                M map = factory.apply(Math.min(count, MAX_INITIAL_COLLECTION_SIZE));
                for (int i = 0; i < count; i++) {
                    K key = keyCodec.decode(buffer);
                    V value = valueCodec.decode(buffer);
                    map.put(key, value);
                }
                return map;
            }

            @Override
            public void encode(B buffer, M value) {
                writeCount(buffer, value.size(), maxSize);
                value.forEach((k, v) -> {
                    keyCodec.encode(buffer, k);
                    valueCodec.encode(buffer, v);
                });
            }
        };
    }

    static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(StreamCodec<? super B, L> leftCodec, StreamCodec<? super B, R> rightCodec) {
        return new StreamCodec<>() {
            @Override
            public Either<L, R> decode(B buffer) {
                return buffer.readBoolean() ? Either.left(leftCodec.decode(buffer)) : Either.right(rightCodec.decode(buffer));
            }

            @Override
            public void encode(B buffer, Either<L, R> value) {
                value.ifLeft(left -> {
                    buffer.writeBoolean(true);
                    leftCodec.encode(buffer, left);
                }).ifRight(right -> {
                    buffer.writeBoolean(false);
                    rightCodec.encode(buffer, right);
                });
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(IntFunction<T> idLookup, ToIntFunction<T> idGetter) {
        return new StreamCodec<>() {
            @Override
            public T decode(ByteBuf buffer) {
                return idLookup.apply(VAR_INT.decode(buffer));
            }

            @Override
            public void encode(ByteBuf buffer, T value) {
                VAR_INT.encode(buffer, idGetter.applyAsInt(value));
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> idMap) {
        return idMapper(idMap::byIdOrThrow, idMap::getId);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(ResourceKey<? extends Registry<T>> registryKey, Function<Registry<T>, IdMap<R>> idGetter) {
        return new StreamCodec<>() {
            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf buffer) {
                return idGetter.apply(buffer.registryAccess().registryOrThrow(registryKey));
            }

            @Override
            public R decode(RegistryFriendlyByteBuf buffer) {
                int id = VAR_INT.decode(buffer);
                return this.getRegistryOrThrow(buffer).byIdOrThrow(id);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, R value) {
                int id = this.getRegistryOrThrow(buffer).getId(value);
                if (id == IdMap.DEFAULT) {
                    throw new EncoderException("Can't find id for '" + value + "' in registry " + registryKey.location());
                }
                VAR_INT.encode(buffer, id);
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> registryKey) {
        return registry(registryKey, registry -> registry);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        return registry(registryKey, Registry::asHolderIdMap);
    }
}
