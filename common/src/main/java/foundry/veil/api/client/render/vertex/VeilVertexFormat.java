package foundry.veil.api.client.render.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VeilVertexFormat {

    public static final VertexFormatElement BONE_INDEX = register(0, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.GENERIC, 1);

    // todo: padding???
    public static final VertexFormat SKINNED_MESH = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0) // texture coordinates
            .put("UV1", DefaultVertexFormat.ELEMENT_UV1) // lightmap coordinates
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2) // overlay coordinates
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .put("BoneIndex", BONE_INDEX)
            .build());
    public static final VertexFormat QUASAR_PARTICLE = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .build());

    /**
     * Creates a new vertex format element.
     * <p>
     * On 1.21 elements are registered into a global id table. 1.20.1 has no such table, so this simply creates the element.
     *
     * @param index The index of the element
     * @param type  The type of data to store
     * @param usage The way the element is used
     * @param count The number of components
     * @return A new element
     */
    public static VertexFormatElement register(int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
        return new VertexFormatElement(index, type, usage, count);
    }
}
