package foundry.veil.forge.mixin.resources;

import foundry.veil.ext.PackResourcesExtension;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.DelegatingPackResources;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Forge 1.20.1 combines all mod resources into a single delegating pack, so expose each mod's pack individually.
 */
@Mixin(value = DelegatingPackResources.class, remap = false)
public class DelegatingPackResourcesMixin implements PackResourcesExtension {

    @Shadow
    @Final
    private List<PackResources> delegates;

    @Override
    public void veil$listResources(PackResourceConsumer consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable IoSupplier<InputStream> veil$getIcon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean veil$blurIcon() {
        return false;
    }

    @Override
    public boolean veil$isStatic() {
        return true;
    }

    @Override
    public List<Path> veil$getRawResourceRoots() {
        return this.delegates.stream().flatMap(pack -> pack instanceof PackResourcesExtension extension ? extension.veil$getRawResourceRoots().stream() : Stream.empty()).toList();
    }

    @Override
    public Stream<PackResources> veil$listPacks() {
        return this.delegates.stream().flatMap(pack -> {
            if (pack instanceof PackResourcesExtension extension) {
                return extension.veil$listPacks();
            } else {
                return Stream.of(pack);
            }
        });
    }
}
