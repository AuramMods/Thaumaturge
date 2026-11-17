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
production mixin injection, networking, worlds, and packaged JAR are **not**
validated by this focused entry point. Neither test archive is a distributable
mod, and normal build/run tasks still include the whole mod.
