package foundry.veil.forge.ext;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;

import java.util.Map;

public interface ShaderChunkRendererExtension {

    void veil$recompile();

    void veil$setActiveBuffers(int activeBuffers);

    Map<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> veil$getPrograms();
}
