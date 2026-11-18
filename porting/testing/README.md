# Background client testing

These development tools are separate from the mod's production source set and
distribution. They do not change Minecraft's normal sound or focus behavior for
players installing the mod.

`./gradlew runClient` prepares `run/client/options.txt` before each launch with
zero master volume, narrator off, fullscreen off, accessibility onboarding off,
and ticking while unfocused. It writes `earlyWindowControl = false` to that
profile's `config/fml.toml`, and attaches the background agent to the test JVM.
The JVM uses `apple.awt.UIElement=true` on the user's Mac.

The agent instruments LWJGL's actual window-creation function to disable initial
focus and focus on show, after the caller sets its hints. It also suppresses
explicit GLFW focus requests within this test JVM. This covers a game or loading
window using that GLFW instance. No shell backgrounding or application activation
is needed.

The window controls can be exercised before the mod compiles:

```sh
./gradlew backgroundAgentJar backgroundSmokeClasses prepareClientProfile
./gradlew runBackgroundWindowSmoke
```

Use a supported Gradle JVM (Java 17 or 21 with the pinned Gradle 8.14.3). On the
current host, commands were run with
`JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`; compilation
and the smoke-test process use the Java 17 toolchain. The native smoke-test task
currently targets macOS Apple Silicon, matching this workspace host.

For an independent read-only observation of foreground application changes:

```sh
xcrun swiftc porting/testing/FocusMonitor.swift -o build/focus-monitor
build/focus-monitor 15 > build/focus.log &
./gradlew runBackgroundWindowSmoke
```

The smoke test creates and shows a small GLFW 3.3.1 window, attempts to request
focus, checks its real focus attributes for three seconds, then destroys the
window and terminates GLFW. It does not initialize audio. The monitor samples
the foreground application's process identity every 20 ms, without activating
or controlling it. A user-initiated app switch also appears in its output and
must be distinguished from test-induced focus changes.

Cycle 004 passed the GLFW test with 56 samples and recorded no foreground-app
changes across 550 monitor samples. This proves the standalone window mechanism
on this host. It does **not** yet prove a full Minecraft/Forge launch, renderer
startup, first-run narration, or all future launcher behavior. Repeat observation
with the actual client when source compilation succeeds, and keep its evidence
in the port tracker.

Do not install the `*-background-test-agent.jar` as a mod or distribute it as the
ported mod. Do not bring Minecraft to the foreground or send global key input
for tests. Use only task-owned test profiles, worlds, and processes.

## Headless Forge foundation tests

```sh
env JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew runForgePortTests --console=plain
```

This launches Forge's actual server userdev class-loading and transformation
pipeline on Java 17, bootstraps Minecraft 1.20.1, then runs the existing
`portCodecTest` classes through JUnit Jupiter. It creates no client window,
initializes no game audio, and starts no world server. Its isolated working
directory is `run/port-tests`; the authorized EULA acceptance is written there.
JUnit XML and a text summary are in `build/test-results/forgePortTest`.
Failures, aborted tests/containers, and an empty executed suite fail the task.

The separate launch-handler archive belongs on Forge's legacy classpath so it
is discovered in the MC-BOOTSTRAP layer alongside its native Forge superclass.
The focused production/test archive is loaded in the transformed Minecraft
layer. Jupiter is selected explicitly because automatic engine discovery across
those module layers failed. `inspectForgePortLaunch` writes resolved native
userdev launch settings to `build/port-testing/server-launch.json` for diagnosis.

Cycle 013 executes all 97 selected tests with zero failures or skips, including
the five registry/context tests that fail before assertions in plain
`portCodecTest`. The normal full source set, full test suite, mod construction,
networking, worlds, and packaged JAR are **not** validated by this focused entry
point. Neither test archive is a distributable mod, and normal build/run tasks
still include the whole mod.

Cycle 020 adds actual entity-tick Mixin injection and runtime fixtures. The
server suite executes 147 tests. It invokes the transformed native
`ServerLevel.tickNonPassenger` method with isolated entity fixtures and the real
Forge event bus, checking cancellation, passenger traversal, ordering,
exceptions and Forge's canUpdate guard.

The client distribution can be tested without starting Minecraft's graphical
entry point:

```sh
env JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew runForgePortClientTests --console=plain
```

This executes the shared suite plus the same runtime cases against transformed
`ClientLevel.tickNonPassenger`: 154 tests in cycle 020. Client-only fixtures are
selected by this task; the server task selects common/server fixtures. Both
distribution launches use the native userdev configurations, resolve their
argument tokens and apply the required focused Mixin config with a generated
refmap. The launch target names include server/client for Mixin side detection.

The client test profile is `run/port-client-tests`, with results under
`build/test-results/forgePortClientTest`. Every launch writes muted sound,
narrator off, fullscreen off, accessibility onboarding off and disabled early
window control, and attaches the background agent. The test entry point does
not create a game window, initialize game audio or start a world server. A
read-only focus monitor recorded no app changes over 517 samples during a
successful cycle 020 client-distribution run.

Cycle 021 adds nine recipe tests on each distribution (156 server / 163 client),
using the actual native RecipeManager, matching, remainders, JSON apply and
serializer network round trips. The runner registers Forge's actual vanilla
ingredient serializer, normally registered by ForgeMod's recipe-serializer
event, so native JSON parsing works without full mod construction.

The focused config currently applies the two entity-tick mixins, recipe
manager lifetime/context mixin, server-resource lookup constructor hook and
connection-owned packet-decoder registry hook and native RegistryOps lookup
accessor. The five earlier persistence/item mixins compile with
the real processor/refmap but are not applied here yet. Full gameplay subscriber
execution, mod recipe serializers/inputs/IDs, live worlds, rendering, all other
mixins and actual distribution installation remain pending.


Cycle 022 expands the focused suites to 171 server / 178 client tests. The new
input/base adapters are exercised through native shaped/shapeless matching and
special-recipe callbacks, including expansion of trimmed remainders to the
original crafting slot. This does not yet validate the full mod recipe classes
or native serialization: the next port step must bind real resource IDs during
Forge JSON/network decoding and supply the appropriate registry context.


Cycle 023 implements the native serializer callbacks and expands the suites to
181 server / 188 client tests. Real fixture types/serializers are registered via
ForgeRegistries. Tests invoke the real server-resource constructor and recipe
JSON apply path, and exercise native-buffer serialization, source-lookup
retention, ID binding, lookup-only providers, malformed data and concurrency.
The callback tests do not establish login packet ordering, a live connection,
all full-mod recipe data, or a complete Forge/mod load.

Cycle 024 adds eight native packet cases (189 server / 196 client). They pass
real Minecraft PacketEncoder/PacketDecoder bodies through Netty EmbeddedChannel
and decode login followed by recipe synchronization before invoking any world
handler. Tests cover received holder identities, different numeric registry IDs
on independent connections, later re-encoding, vanilla recipes and keepalives,
the serverbound path, missing login registries, unknown holder IDs and native
trailing-byte rejection. The fifth focused production mixin composes a lookup
from the validated login's received registries and actual built-in registries,
owned by that decoder instance. Packet validation and exceptions remain native.
This is packet-body codec evidence; it does not exercise TCP, compression,
encryption, Forge handshake/mod construction, live worlds or multiplayer clients.

Cycle 025 adds eight codec comparison/concurrency tests (197 server / 204 client).
The owned DFU 6 adapters are compared to 189 observations generated by the actual
Minecraft 1.21.1 DFU 8.0.16 library in a separate JVM/classpath. The tracked
reference and regeneration procedure are described in `dfu-reference/README.md`.
These exercise JSON codecs and controlled schemas; complete mod data, native NBT
loading and gameplay acceptance remain pending.

Cycle 026 preserves strict and explicitly lenient optional-field semantics at
399 production call sites. The reference grows to 297 observations in eight
categories, with 108 optional-field cases including compressed JSON. Together
with a production item-data JSON/native-NBT regression, the final native suites
execute 199 server / 206 client tests. Defaults still apply to absent fields;
malformed strict fields retain the baseline errors/partials instead of becoming
successful defaults. Complete mod data and world persistence remain pending.

Cycle 027 adapts the nine MapCodec dispatch/partial-dispatch consumers and adds
76 real-library reference observations. The focused runtime now includes the
actual BlueprintSource/BlueprintTarget classes and four cases for their flat
and compressed JSON, strict flags/defaults and all eight variant wire codecs.
StackTarget's JSON item schema remains pending the item-stack codec conversion.
The runner invokes Forge's actual BLOCKS registry bake after vanilla bootstrap
to populate native block-state IDs/caches; no synthetic state-ID table is used.
Final counts are 205 server / 212 client tests. These remain codec tests without
full mod construction, blueprint world transactions or a live game world.

Cycle 028 adds the ingredient codec entry point backed by actual Forge parsers,
registered serializers and CompoundIngredient. Eight native cases cover item,
tag and empty schemas, alternatives and error partials/remainders, malformed
input, native NBT, encoder prefixes, and real non-simple Forge predicates through
JSON and the native ingredient wire codec. Final counts are 213 server / 220
client tests. Component-sensitive and spell-part ingredient serializers remain
pending; the full mod's recipe bodies and progression are not established by
these fixtures.

Cycle 029 adds the native codec ingredient serializer/registration backend and
component-sensitive ingredient. Fifteen new cases bring the suites to 228
server / 235 client tests. They exercise actual serializer events and native
shapeless matching, strict/subset data, item tags, JSON/NBT/wire booleans and
holders, registry ownership and exception cleanup. The sixth focused mixin
exposes RegistryOps' actual lookup so construction can persist item data with
the original lookup while changing the parent ops to NBT. No substitute registry
provider is used. The full mod's spell-part implementation, actual mod component
catalog, potion data backend and complete recipes/progression remain pending.

Cycle 030 adds actual ArcaneShapedRecipePattern matching/serialization tests,
including trimming, mirroring, holes, invalid/partial key data, wire layout and
configured grid expansion. An all-space pattern crash is now a data error.
The shared strict-map and getOrThrow bridges are compared to the actual 1.21.1
game/DFU libraries. Final suites execute 237 server / 244 client tests. These
pattern tests use a controlled workbench view over the real crafting input;
crystal/vis payment, research, transactions and world crafting remain pending.


Cycle 031 adds named effect codecs, native effect NBT and actual client packet
handler tests. The common effect mixin and client unsigned-amplifier mixin are
applied through Forge's real transformation pipeline. The focused totals are
246 server / 254 client; these controlled fixtures do not start a world or a
multiplayer connection. `generateEffectDetailsReference` runs the actual pinned
1.21.1 private Details codec in the isolated Java 21 reference JVM; normal tests
compare its ten tracked observations using only the target Forge/DFU runtime.


Cycle 032 adds twelve native potion-data cases and one component-ingredient case,
including actual PotionUtils interoperability, native ItemStack NBT save/load,
copy/removal/default semantics, component patches, colors and recipe matching.
The focused totals are 259 server / 267 client. Native parser fixtures supply
real registry ownership; no actual world potion consumption is claimed.


Cycle 033 adds actual native brewing-table and Mix accessor tests plus custom
registered potion route cases. Eight common and two client mixins execute in
the focused launch, with 264 server / 272 client tests passing. Native brewing
and route inspection run without worlds, windows or audio; sprayer world
transactions and aspect costs still require full-mod runtime acceptance.


Cycle 034 adds actual DeferredRegister/ForgeRegistration event tests with native
block/item registry lifecycle, predicates, recipe generation and creative output.
The fixture thaws and refreezes both native views as Forge's registration phase
does, then bakes block states. The focused totals are 265 server / 273 client;
full mod catalog construction and all changed consumer paths remain pending.


Cycle 035 verifies every vanilla effect constant against actual bound built-in
holders, Forge delegate keys/values and named-codec round trips. Built-in and
Forge delegate references can be distinct objects. Focused totals are now
266 server / 274 client; full effect gameplay and the mod catalog remain pending.


Cycle 036 drives actual source custom effect classes through native effect tick
dispatch, with controlled entity/world services, covering periodic and infinite
timing, exhaustion, fire/healing branches and marker expiration. Focused totals
are 273 server / 281 client. These tests do not run a world or prove the wider
taint/physical-flux dependencies or full mod catalog construction.


Cycle 037 verifies native deferred effect registration and lazy command-style
supplier maps, including real registry identity and named effect round trips.
Focused totals are 274 server / 282 client. Full effect catalog and automatic
command subscriber construction still need full-mod runtime validation.


Cycle 038 compares the packed-color bridge against 1,423 actual pinned 1.21.1
observations, generated by `generateColorReference` in an isolated Java 21 JVM.
Five groups also execute through native Forge, bringing focused totals to
279 server / 287 client. This proves pixel math; visual rendering and the full
client still need acceptance. Reference-only logging dependencies do not enter
the Forge game classpath.


Cycle 039 checks named and direct sound holder codecs and native vanilla sound
packet parameters without opening a sound engine or audio device. Focused totals
are 282 server / 290 client; actual spell/equipment execution and full-client
sound resource validation remain pending under the silent test profiles.


Cycle 040 verifies an actual Forge armor-material registry, native armor item
construction before material binding, stats/attributes, per-item durability,
repair, layer metadata and damage persistence. Focused totals are 288 server /
296 client. Full material catalog/tool tiers and multi-layer/tint/trims/rendering
still require their remaining source and full-mod runtime acceptance.
