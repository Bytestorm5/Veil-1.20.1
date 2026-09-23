package foundry.veil.forge.compat.sodium;

import foundry.veil.ext.sodium.ChunkVertexEncoderVertexExtension;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttribute;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;

/**
 * Embeddium "compact" chunk vertex format with an additional packed normal.
 * <p>
 * The first {@value #COMPACT_STRIDE} bytes are exactly Embeddium's {@link CompactChunkVertex} layout (written by
 * delegating to its encoder), followed by 4 signed bytes of normal data at offset {@value #COMPACT_STRIDE}.
 * Embeddium keys its vertex format on the fixed {@link ChunkMeshAttribute} enum, so the extra normal attribute is
 * exposed separately through {@link #NORMAL_ATTRIBUTE} and bound to attribute index {@value #NORMAL_ATTRIBUTE_INDEX}
 * by {@code DefaultChunkRendererMixin}.
 */
public class VeilChunkVertex implements ChunkVertexType {

    public static final int COMPACT_STRIDE = 20;
    public static final int STRIDE = COMPACT_STRIDE + 4;
    public static final int NORMAL_ATTRIBUTE_INDEX = 4;

    public static final GlVertexFormat<ChunkMeshAttribute> VERTEX_FORMAT = GlVertexFormat.builder(ChunkMeshAttribute.class, STRIDE)
            .addElement(ChunkMeshAttribute.POSITION_MATERIAL_MESH, 0, GlVertexAttributeFormat.UNSIGNED_SHORT, 4, false, true)
            .addElement(ChunkMeshAttribute.COLOR_SHADE, 8, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, true, false)
            .addElement(ChunkMeshAttribute.BLOCK_TEXTURE, 12, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false, false)
            .addElement(ChunkMeshAttribute.LIGHT_TEXTURE, 16, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false, true)
            .build();
    public static final GlVertexAttribute NORMAL_ATTRIBUTE = new GlVertexAttribute(new GlVertexAttributeFormat(GL11C.GL_BYTE, 1), 4, false, COMPACT_STRIDE, STRIDE, true);

    private final ChunkVertexType compact;
    private final ChunkVertexEncoder compactEncoder;

    public VeilChunkVertex() {
        this.compact = new CompactChunkVertex();
        this.compactEncoder = this.compact.getEncoder();
    }

    @Override
    public float getPositionScale() {
        return this.compact.getPositionScale();
    }

    @Override
    public float getPositionOffset() {
        return this.compact.getPositionOffset();
    }

    @Override
    public float getTextureScale() {
        return this.compact.getTextureScale();
    }

    @Override
    public GlVertexFormat<ChunkMeshAttribute> getVertexFormat() {
        return VERTEX_FORMAT;
    }

    @Override
    public ChunkVertexEncoder getEncoder() {
        return (ptr, material, vertex, sectionIndex) -> {
            this.compactEncoder.write(ptr, material, vertex, sectionIndex);
            MemoryUtil.memPutInt(ptr + COMPACT_STRIDE, ((ChunkVertexEncoderVertexExtension) vertex).veil$getPackedNormal());
            return ptr + STRIDE;
        };
    }
}
