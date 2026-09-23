package foundry.veil.api.client.render.post.uniform;

import foundry.veil.api.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;

import java.util.Arrays;

public record IntUniform(int[] values) implements UniformValue {

    public static final MapCodec<IntUniform> CODEC = CodecUtil.listOf(Codec.INT, 1, 4)
            .xmap(floats -> new IntUniform(floats.stream().mapToInt(Integer::intValue).toArray()),
                    uniform -> Arrays.stream(uniform.values).boxed().toList()).fieldOf("value");

    @Override
    public void apply(ShaderUniform uniform) {
        uniform.setVectorI(this.values);
    }

    @Override
    public Type type() {
        return Type.INT;
    }
}
