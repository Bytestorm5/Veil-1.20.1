package foundry.veil.backport.network.codec;

import foundry.veil.backport.network.codec.VanillaStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static foundry.veil.backport.network.codec.ByteBufCodecs.wrap;

/**
 * The {@code STREAM_CODEC} constants 1.21 declares on vanilla classes, which can't be added to those classes on 1.20.1.
 * For example {@code VanillaStreamCodecs.RESOURCE_LOCATION} is {@link #RESOURCE_LOCATION}.
 */
public final class VanillaStreamCodecs {

    public static final StreamCodec<ByteBuf, ResourceLocation> RESOURCE_LOCATION = StreamCodec.of((buf, value) -> wrap(buf).writeResourceLocation(value), buf -> wrap(buf).readResourceLocation());
    public static final StreamCodec<ByteBuf, UUID> UUID = StreamCodec.of((buf, value) -> wrap(buf).writeUUID(value), buf -> wrap(buf).readUUID());
    public static final StreamCodec<ByteBuf, BlockPos> BLOCK_POS = StreamCodec.of((buf, value) -> wrap(buf).writeBlockPos(value), buf -> wrap(buf).readBlockPos());
    public static final StreamCodec<ByteBuf, ChunkPos> CHUNK_POS = StreamCodec.of((buf, value) -> buf.writeLong(value.toLong()), buf -> new ChunkPos(buf.readLong()));
    public static final StreamCodec<ByteBuf, SectionPos> SECTION_POS = StreamCodec.of((buf, value) -> buf.writeLong(value.asLong()), buf -> SectionPos.of(buf.readLong()));
    public static final StreamCodec<ByteBuf, GlobalPos> GLOBAL_POS = StreamCodec.of((buf, value) -> wrap(buf).writeGlobalPos(value), buf -> wrap(buf).readGlobalPos());
    public static final StreamCodec<ByteBuf, Direction> DIRECTION = ByteBufCodecs.idMapper(Direction::from3DDataValue, Direction::get3DDataValue);
    public static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.of((buf, value) -> {
        buf.writeDouble(value.x);
        buf.writeDouble(value.y);
        buf.writeDouble(value.z);
    }, buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    public static final StreamCodec<ByteBuf, Component> COMPONENT = StreamCodec.of((buf, value) -> wrap(buf).writeComponent(value), buf -> wrap(buf).readComponent());
    public static final StreamCodec<ByteBuf, ItemStack> ITEM_STACK = StreamCodec.of((buf, value) -> wrap(buf).writeItem(value), buf -> wrap(buf).readItem());
    public static final StreamCodec<FriendlyByteBuf, FriendlyByteBuf> TRAILING_BYTES = StreamCodec.of((buf, value) -> buf.writeBytes(value.copy()), buf -> new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));

    private VanillaStreamCodecs() {
    }
}
