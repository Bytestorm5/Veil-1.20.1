package foundry.veil.impl.client.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implementations of rendering helpers that exist on 1.21 vanilla classes but not on 1.20.1.
 */
@ApiStatus.Internal
public final class BackportRenderHelper {

    private BackportRenderHelper() {
    }

    /**
     * Equivalent of 1.21's {@code ShaderInstance#setDefaultUniforms}. Mirrors the uniform setup 1.20.1 performs in
     * {@code VertexBuffer#_drawWithShader}.
     */
    public static void setDefaultUniforms(ShaderInstance shader, VertexFormat.Mode mode, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Window window) {
        for (int i = 0; i < 12; ++i) {
            int id = RenderSystem.getShaderTexture(i);
            shader.setSampler("Sampler" + i, id);
        }

        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(modelViewMatrix);
        }
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(projectionMatrix);
        }
        if (shader.INVERSE_VIEW_ROTATION_MATRIX != null) {
            shader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }
        if (shader.COLOR_MODULATOR != null) {
            shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        if (shader.GLINT_ALPHA != null) {
            shader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }
        if (shader.FOG_START != null) {
            shader.FOG_START.set(RenderSystem.getShaderFogStart());
        }
        if (shader.FOG_END != null) {
            shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.FOG_COLOR != null) {
            shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        if (shader.FOG_SHAPE != null) {
            shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }
        if (shader.TEXTURE_MATRIX != null) {
            shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        if (shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float) window.getWidth(), (float) window.getHeight());
        }
        if (shader.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
            shader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }

        RenderSystem.setupShaderLights(shader);
    }

    /**
     * Equivalent of 1.21's {@code VertexFormat#getOffset}.
     *
     * @return The byte offset of the element in the vertex, or <code>-1</code> if the format doesn't contain it
     */
    public static int getOffset(VertexFormat format, VertexFormatElement element) {
        int offset = 0;
        for (VertexFormatElement e : format.getElements()) {
            if (e.equals(element)) {
                return offset;
            }
            offset += e.getByteSize();
        }
        return -1;
    }

    /**
     * @return The byte offset of the element at the specified index in the format
     */
    public static int getOffset(VertexFormat format, int elementIndex) {
        List<VertexFormatElement> elements = format.getElements();
        int offset = 0;
        for (int i = 0; i < elementIndex; i++) {
            offset += elements.get(i).getByteSize();
        }
        return offset;
    }

    /**
     * Equivalent of 1.21's {@code VertexFormat#getElementName}.
     */
    public static @Nullable String getElementName(VertexFormat format, VertexFormatElement element) {
        List<VertexFormatElement> elements = format.getElements();
        List<String> names = format.getElementAttributeNames();
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).equals(element)) {
                return names.get(i);
            }
        }
        return null;
    }

    /**
     * Equivalent of 1.21's {@code Tesselator#begin}. Starts building with the tesselator's shared buffer.
     */
    public static BufferBuilder begin(Tesselator tesselator, VertexFormat.Mode mode, VertexFormat format) {
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(mode, format);
        return builder;
    }

    /**
     * Equivalent of 1.21's {@code BufferBuilder#build}, which returns <code>null</code> if no vertices were added.
     */
    public static @Nullable BufferBuilder.RenderedBuffer build(BufferBuilder builder) {
        BufferBuilder.RenderedBuffer buffer = builder.end();
        if (buffer.isEmpty()) {
            buffer.release();
            return null;
        }
        return buffer;
    }

    /**
     * Equivalent of 1.21's {@code BufferBuilder#buildOrThrow}.
     */
    public static BufferBuilder.RenderedBuffer buildOrThrow(BufferBuilder builder) {
        BufferBuilder.RenderedBuffer buffer = builder.end();
        if (buffer.isEmpty()) {
            buffer.release();
            throw new IllegalStateException("BufferBuilder was empty");
        }
        return buffer;
    }

    /**
     * Equivalent of 1.21's {@code RenderType#draw(MeshData)}.
     */
    public static void draw(RenderType renderType, BufferBuilder.RenderedBuffer buffer) {
        renderType.setupRenderState();
        BufferUploader.drawWithShader(buffer);
        renderType.clearRenderState();
    }

    /**
     * Equivalent of 1.21's {@code MeshData#indexBuffer}, which is <code>null</code> when the mesh uses a sequential index buffer.
     */
    public static @Nullable ByteBuffer indexBuffer(BufferBuilder.RenderedBuffer buffer) {
        return buffer.drawState().sequentialIndex() ? null : buffer.indexBuffer();
    }
}
