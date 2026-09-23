package foundry.veil.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NativeResource;

import javax.annotation.Nullable;

public class CachedBufferSource implements MultiBufferSource, NativeResource {

    private final Object2ObjectMap<RenderType, BufferBuilder> buffers = new Object2ObjectArrayMap<>();
    private final Object2ObjectMap<RenderType, BufferBuilder> startedBuilders = new Object2ObjectArrayMap<>();

    @Nullable
    private RenderType lastSharedType;

    private void clearBuffers() {
        // 1.20.1 buffer builders own their memory and have no explicit free
        this.buffers.clear();
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        BufferBuilder last = this.startedBuilders.get(renderType);
        if (last != null && !renderType.canConsolidateConsecutiveGeometry()) {
            this.endBatch(renderType, last);
            last = null;
        }

        if (last != null) {
            return last;
        }

        BufferBuilder builder = this.buffers.computeIfAbsent(renderType, unused -> new BufferBuilder(renderType.bufferSize()));
        builder.begin(renderType.mode(), renderType.format());
        this.startedBuilders.put(renderType, builder);
        return builder;
    }

    @Override
    public void free() {
        this.clearBuffers();
        this.startedBuilders.clear();
        this.lastSharedType = null;
    }

    public void endLastBatch() {
        if (this.lastSharedType != null) {
            this.endBatch(this.lastSharedType);
            this.lastSharedType = null;
        }
    }

    public void endBatch() {
        this.endLastBatch();

        for (RenderType rendertype : this.buffers.keySet()) {
            this.endBatch(rendertype);
        }

        this.clearBuffers();
    }

    public void endBatch(RenderType renderType) {
        BufferBuilder bufferbuilder = this.startedBuilders.remove(renderType);
        if (bufferbuilder != null) {
            this.endBatch(renderType, bufferbuilder);
        }
    }

    private void endBatch(RenderType renderType, BufferBuilder builder) {
        // Sorts quads if the render type requires it, then draws the buffer
        renderType.end(builder, RenderSystem.getVertexSorting());

        if (renderType.equals(this.lastSharedType)) {
            this.lastSharedType = null;
        }
    }
}
