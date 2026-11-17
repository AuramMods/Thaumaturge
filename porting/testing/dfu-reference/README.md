# Codec reference comparison

`generateDfuReference` compiles and runs `CodecReferenceProbe` against Mojang's
DataFixerUpper **8.0.16**, the library shipped in Minecraft 1.21.1. The generated
fixture is tracked at
`src/test/resources/com/leclowndu93150/thaumaturge/serialization/dfu8-reference.json`.

The same probe source is compiled against Forge 1.20.1's DFU **6.0.8** in the
normal/focused test source sets. `CodecsTest` compares the owned compatibility
operations to the recorded reference, including result/partial value,
rejected-input remainder, error message, lifecycle, element-call counts and
lazy initialization counts. No DFU 8 classes enter the mod or Forge test runtime.

```sh
env JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew generateDfuReference --console=plain
env JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew portCodecTest --tests com.leclowndu93150.thaumaturge.serialization.CodecsTest --console=plain
```

Reference dependencies use the Minecraft 1.21.1 versions: Gson 2.10.1, Guava
32.1.2-jre and fastutil 8.5.12. Regeneration is explicit; regular tests use the
committed fixture and do not rewrite their expected values.

The official baseline artifact was downloaded from
[Mojang libraries](https://libraries.minecraft.net/com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar)
and verified against Minecraft 1.21.1's version metadata:
SHA-1 `67d4de6d7f95d89bcf5862995fb854ebaec02a34`.
The cycle-025 evidence records that metadata and the actual reference bytecode.
Adapted production portions retain Microsoft's MIT notice in
`src/main/resources/META-INF/licenses/thaumaturge-dfu-compat.txt`.

The DFU fixture contains 376 observations in eleven categories. It exercises
JSON ops and controlled schemas, not every mod codec or native NBT/world load.
The optional-field comparisons include strict/lenient decoding, defaults,
partial/error preservation, field omission and compressed JSON. A separate
production item-data test covers malformed and valid native NBT fields.
Dispatch/partial-dispatch comparisons also cover flat and compressed schemas,
unknown types, malformed fields, encoder type/codec errors and partial results.
The truncated compressed-payload case records and compares the actual exception
class (both libraries throw); no production exception handling is replaced.
Several newer vanilla ingredient/item schemas still require conversion.

The separate `strict-map-1.21.1-reference.json` fixture contains 13 observations
from the actual Minecraft 1.21.1 `ExtraCodecs.StrictUnboundedMapCodec` class. The
probe loads its pinned obfuscated class (`axw$b`) without booting Minecraft.
It checks map errors, partial rejection, lifecycle, remainder and encoding. The
DFU fixture also checks no-argument getOrThrow success and both error modes.

To regenerate the strict-map reference, use the official 1.21.1 client artifact
(SHA-1 `30c73b1c5da787909b2f73340419fdf13b9def88`) with a Java 21 Gradle JVM:

```sh
curl --fail --location https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar --output /tmp/thaumaturge-reference-1.21.1.jar
env JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew generateStrictMapReference -PportReferenceMinecraftJar=/tmp/thaumaturge-reference-1.21.1.jar --console=plain
```

The default reference path uses the existing cycle-011 evidence artifact.
Only explicit regeneration needs that JAR; normal tests use the tracked fixture.
Neither Minecraft 1.21.1 nor DFU 8 enters Forge's game/test runtime classpath.
