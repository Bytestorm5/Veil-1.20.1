package foundry.veil.backport.resources;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Backport of the static {@code ResourceLocation} factories added in 1.21.
 */
public final class ResourceLocations {

    private ResourceLocations() {
    }

    /**
     * Equivalent of {@code ResourceLocation.fromNamespaceAndPath}.
     */
    public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    /**
     * Equivalent of {@code ResourceLocation.parse}.
     *
     * @throws ResourceLocationException If the location is invalid
     */
    public static ResourceLocation parse(String location) {
        return new ResourceLocation(location);
    }

    /**
     * Equivalent of {@code ResourceLocation.withDefaultNamespace}.
     */
    public static ResourceLocation withDefaultNamespace(String path) {
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
    }

    /**
     * Equivalent of {@code ResourceLocation.tryParse}.
     */
    public static @Nullable ResourceLocation tryParse(String location) {
        return ResourceLocation.tryParse(location);
    }

    /**
     * Equivalent of {@code ResourceLocation.tryBuild}.
     */
    public static @Nullable ResourceLocation tryBuild(String namespace, String path) {
        return ResourceLocation.tryBuild(namespace, path);
    }
}
