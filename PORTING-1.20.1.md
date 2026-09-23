# Veil 4.3.2 on Forge 1.20.1

This branch is a backport of Veil 4.3.2 (the `1.21` branch at `mc1.21.1-4.3.2`) to Minecraft 1.20.1 and Forge 47.
It keeps the 4.x public API, so mods written against Veil 4.3.2 (Sable, Simulated) can be ported without rewriting
their Veil calls. Where Veil's API exposes a vanilla type that only exists on 1.21, that type was either backported
(see [Backported types](#backported-types)) or swapped for its 1.20.1 equivalent (see [API differences](#api-differences-from-veil-432-on-1211)).

## Building

- `./gradlew build` produces `forge/build/libs/veil-forge-1.20.1-<version>.jar` (reobfuscated to SRG, with the
  refmap, SRG access transformer and jar-in-jar dependencies).
- The build uses ModDevGradle's `legacyforge` plugin. It compiles with a Java 21 toolchain but emits Java 17 bytecode
  against the Java 17 API (`--release 17`), because ImGuiMC (compile-only) is only published for Java 21.
- The Fabric module was dropped; Forge is the only 1.20.1 target.
- `common/src/main/resources/META-INF/accesstransformer.cfg` uses SRG names, as Forge 1.20.1 requires. The
  mojmap name of every entry is kept as a trailing comment.

Dev run options:

| Property             | Effect                                                              |
|----------------------|---------------------------------------------------------------------|
| `-PwithCompatMods`   | Adds Embeddium and Oculus to the dev runs                           |
| `-PquickPlay=<save>` | Loads straight into a singleplayer world (`runClient` only)         |

## Backported types

Forge 1.20.1 loads Minecraft as its own module, so types can't be added to `net.minecraft.*` packages. Each backported
type keeps its 1.21 name and API under `foundry.veil.backport` at the same relative package:

| 1.21 type                                                          | Veil 1.20.1 type                                                      |
|--------------------------------------------------------------------|-----------------------------------------------------------------------|
| `net.minecraft.network.codec.StreamCodec` (+ `StreamEncoder`, `StreamDecoder`, `StreamMemberEncoder`, `ByteBufCodecs`) | `foundry.veil.backport.network.codec.*` |
| `net.minecraft.network.RegistryFriendlyByteBuf`                    | `foundry.veil.backport.network.RegistryFriendlyByteBuf`               |
| `net.minecraft.network.protocol.common.custom.CustomPacketPayload` | `foundry.veil.backport.network.protocol.common.custom.CustomPacketPayload` |
| `net.minecraft.client.DeltaTracker`                                | `foundry.veil.backport.client.DeltaTracker`                           |
| `com.mojang.blaze3d.vertex.ByteBufferBuilder`                      | `foundry.veil.backport.blaze3d.vertex.ByteBufferBuilder`              |
| `X.STREAM_CODEC` constants on vanilla classes (`ResourceLocation`, `UUIDUtil`, `BlockPos`, ...) | `foundry.veil.backport.network.codec.VanillaStreamCodecs` |
| `ResourceLocation.fromNamespaceAndPath` / `parse` / `withDefaultNamespace` | `foundry.veil.backport.resources.ResourceLocations`           |

Code written against 1.21 can be ported by rewriting imports, for example:

```sh
sed -i \
  -e 's/import net\.minecraft\.network\.codec\./import foundry.veil.backport.network.codec./' \
  -e 's/import net\.minecraft\.network\.RegistryFriendlyByteBuf;/import foundry.veil.backport.network.RegistryFriendlyByteBuf;/' \
  -e 's/import net\.minecraft\.network\.protocol\.common\.custom\./import foundry.veil.backport.network.protocol.common.custom./' \
  -e 's/import net\.minecraft\.client\.DeltaTracker;/import foundry.veil.backport.client.DeltaTracker;/' \
  $(find src -name '*.java')
```

Tag codecs (`ByteBufCodecs.TAG` and the `fromCodec*` family) wrap arbitrary tags in a compound, because 1.20.1's
`FriendlyByteBuf` can only read compound tags. Both sides use Veil's codecs, so this is transparent.

## Networking

`VeilPacketManager` keeps its 4.x API. On Forge each payload type gets its own `EventNetworkChannel` named after the
payload id. Payloads are encoded with the registered `StreamCodec` into a vanilla custom payload packet, and bundling
multiple payloads still uses `ClientboundBundlePacket`. Optional payloads use `NetworkRegistry.acceptMissingOr`, so
clients or servers without the mod can still connect. Handlers run on the main thread, as on NeoForge.

## API differences from Veil 4.3.2 on 1.21.1

| API                                                                 | 1.20.1 change                                                                                |
|---------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| `VertexArray.upload(MeshData, ...)`, `uploadIndexBuffer(MeshData.DrawState)` | Take `BufferBuilder.RenderedBuffer` / `BufferBuilder.DrawState`. `upload` releases the buffer. |
| `RenderStyle#createMesh`, `InstancedLightRenderer#createMesh`       | Return `BufferBuilder.RenderedBuffer`                                                          |
| `VeilRenderType.LayeredRenderType` / `RenderTypeWrapper`            | Override `RenderType#end(BufferBuilder, VertexSorting)` instead of `draw(MeshData)`             |
| `CachedBufferSource`                                                | Uses one `BufferBuilder` per render type (1.20.1 builders own their memory)                    |
| `Skin#render(..., ByteBufferBuilder, ...)`                          | Takes the backported `ByteBufferBuilder`                                                       |
| `VeilVertexFormat.register`                                         | Creates the element directly. 1.20.1 has no global element id table                            |
| `VeilRenderLevelStageEvent` / `VeilLevelPerspectiveRenderer`        | Use the backported `DeltaTracker`. `DeltaTracker.current()` replaces `Minecraft#getTimer()`    |
| `VeilPlatform`                                                      | Adds `PlatformType.FORGE`, `isClient()` and `getServer()`                                      |
| `CodecUtil`                                                         | Adds `listOf(codec, min, max)`, `withAlternative`, `getOrThrow`, `isSuccess` and `isError`, which DFU 6 lacks |
| Registry-based dispatch types (`ModuleType`, `PipelineType`, ...)   | Still return `MapCodec`. The dispatch codecs call `MapCodec#codec()`, which DFU 6 inlines        |
| `VeilDynamicRegistry`                                               | `RegistryData` has no `requiredNonEmpty` flag on 1.20.1                                          |

1.21-only vanilla behaviour that doesn't exist on 1.20.1:

- The GUI sprite atlas (1.20.2+) doesn't exist. Texture hot reloading checks the mob effect and painting atlases instead.
- `NativeImage` doesn't validate PNG headers on 1.20.1, so the mixin that disabled that check was removed.
- There is no `ShaderInstance#setDefaultUniforms`. The equivalent lives in `BackportRenderHelper.setDefaultUniforms`,
  and Veil's extra default uniforms (`VeilRenderTime`, `NormalMat`, `VeilBlockFaceBrightness`) are set when a shader is applied.
- Chunk rendering is retargeted from `SectionRenderDispatcher` to `ChunkRenderDispatcher.RenderChunk` and
  `LevelRenderer#renderChunkLayer`, including the level perspective renderer's occlusion graph.

## Compatibility

- Sodium and Iris compatibility targets **Embeddium 0.3.31** and **Oculus 1.8.0**, the Forge 1.20.1 forks. `SodiumCompat` and
  `IrisCompat` keep their public API. Embeddium's terrain vertex format gains a 4-byte normal (24-byte stride) the
  same way Veil extends Sodium's format on 1.21.
- ImGuiMC has no 1.20.1 build, so the ImGui editor is compiled but inactive unless an ImGuiMC port is installed.

## Verification

- Static check of every Minecraft-targeting mixin (target class, `method`, `@At` target, `@Shadow`, `@Accessor`/`@Invoker`,
  `@Local` capture and `@Inject` handler parameters) against the 1.20.1 Forge game jar.
- Dev client (Mesa software GL, Xvfb): title screen and a singleplayer world render. `/veil post_processing add`
  round-trips a Veil payload from server to client and applies a Veil post pipeline.
- Dev client with Embeddium and Oculus: terrain renders through Embeddium with Veil's vertex format, post
  processing works, and enabling all dynamic buffers recompiles Embeddium's chunk shaders without errors.
- Production Forge 1.20.1 dedicated server (installed with the official installer, SRG runtime) loads the built jar.

Not verified: a production (SRG) client, Oculus with a shader pack loaded, and ImGui editor features.
