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
manager lifetime/context mixin and server-resource lookup constructor hook. The five earlier persistence/item mixins compile with
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
