# Thaumaturge Forge 1.20.1 port tracker

## Objective and current status

Port the complete Thaumaturge feature set at the pinned 1.21.1 baseline to Minecraft **1.20.1 on Forge**, preserving gameplay, progression, content, visuals, persistence, and multiplayer behavior. Compilation or reaching the title screen does not constitute completion.

**Status: implementation in progress.** The Forge build resolves target dependencies and reaches Java 17 source compilation. Compilation currently fails on unresolved NeoForge and newer Minecraft APIs; no game/server has been launched. All runtime acceptance remains pending.

## Baseline

| Item | Observed value |
| --- | --- |
| Investigation date | 2026-11-17 |
| Repository | `https://github.com/AuramMods/Thaumaturge.git` |
| Working branch | `1.20.1`, created directly from `origin/1.21.1` without tracking that source branch |
| Pinned source commit | `999486b1cff8e34094ed870157657e07adc5f743` (`bump to 1.1.1`) |
| Source platform | Minecraft 1.21.1, NeoForge 21.1.238, Java 21 |
| Source mod version | 1.1.1 |
| Other branch | `main` targets Minecraft 26.1.2 / NeoForge and is farther from the requested target |
| Build | Prism 0.6.4, Gradle wrapper 9.7.0, Spotless 8.9.0 |
| Source inventory | 2,330 main Java files; approximately 254,000 lines |
| Test inventory | 317 test Java files; 1,079 `@Test` and 15 `@ParameterizedTest` annotations (not executed-test counts) |
| Networking / mixins | 42 `playToClient` / `playToServer` registrations in `TTPayloads`; 32 mixin Java files |
| Repository instructions | No repository/ancestor `AGENTS.md`, `docs/MANUAL_CHANGE_PROTOCOL.md`, or root `.codegraph/` found during initial inspection; user-supplied instructions still apply |

The repository ignores `docs`, so the tracked port documents live at the root. Preserve `LICENSE`, `CREDITS.md`, registry identities, and source history. Do not redirect the port to `main` or continuously incorporate moving upstream changes without a concrete reason recorded here.

The official Forge download page listed **47.4.29 latest** and **47.4.28 recommended** for 1.20.1 when inspected. Select and pin an actual tested Forge 47.x release during implementation. Forge's 1.20.1 documentation specifies Java 17; target Java 17 bytecode and runtime compatibility. Build-tool JVM requirements may differ and need a compatible wrapper/plugin combination.

Sources: [Forge downloads](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html), [Forge 1.20.1 setup](https://docs.minecraftforge.net/en/1.20.1/gettingstarted/).

## Conversion map

Paths below are relative to `src/main/java/com/leclowndu93150/thaumaturge/` unless otherwise stated. Counts are text-search indicators, not confirmed compiler errors or effort estimates.

| Area | Evidence in baseline | Required work and verification |
| --- | --- | --- |
| Build and distribution | Root Gradle files use Prism's NeoForge DSL and `NeoForgeExtension`; CI selects Java 25 while the game toolchain is 21 | Establish Forge 1.20.1 development, datagen, test, client, server, and reobfuscated distribution tasks. Use compatible Gradle/JDK/plugins; replace Prism if necessary. Fix CI and metadata without publishing. |
| Loader and registrations | `Thaumaturge.java`, `registry/`, config/event subscriptions; 464 source files mention NeoForge APIs | Convert mod construction, event buses, deferred registrations, configs, sided initialization, attributes, capabilities, and lifecycle hooks to actual Forge semantics. |
| Persistent and transient state | `registry/TTAttachments.java`: knowledge, aspect pools, warp, aura, seals, wards, locks, labyrinth state, spell queues, and more | Replace attachments with appropriate Forge capabilities, saved data, or owned fields. Preserve defaults, serialization, dirty marking, invalidation, copy-on-death, synchronization recipients/timing, and transient-state cleanup. |
| Item state | `registry/TTDataComponents.java`; 107 files mention component APIs or the mod's component registry | Implement 1.20.1 NBT/capability-backed item state. Preserve item copies, stacks, crafting results, inventories, drops, tooltips, sync, defaults, and existing mod-level legacy readers. |
| Networking | `network/TTPayloads.java`, `network/`, `client/network/`; 175 files mention modern payload/stream-codec/buffer APIs | Replace NeoForge payload registration and modern buffers/codecs with Forge networking. Preserve protocol checks, directions, thread scheduling, server-side validation, tracking, login/rejoin, dimension transfers, and client-only class isolation. |
| Capabilities and automation | `registry/TTCapabilities.java`, `api/capability/`, `api/essentia/`, machine/block entity code | Port item/fluid/energy/custom capabilities with correct sided access and provider lifecycle. Exercise hoppers, pipes, storage, inventories, and unloaded/reloaded chunks. |
| Data maps and custom registries | `registry/TTDataMaps.java`, `registry/TTDatapackRegistries.java`, `data/datamap/` | Reimplement reloadable data-map behavior and required client synchronization; preserve tag matching, overrides, defaults, custom research/aspect/spell/worldgen registries, and reload behavior. |
| Recipes and generated data | `content/recipe/SimpleRecipeSerializer.java`, `content/recipe/`, `data/`; 323 files mention `RecipeInput`, `RecipeHolder`, or `MapCodec` | Adapt serializers and container/recipe APIs; convert item-stack JSON, conditions, tags, loot, advancements, registries, and 1.21 directory/schema differences. Regenerate and test packaged resources. |
| Java and vanilla APIs | Java 21 mixin level; 42 files contain newer API/syntax candidates | Audit Java 17 compatibility and vanilla signature changes throughout the source. Port newer vanilla content references deliberately, preserving mod behavior. |
| Client and rendering | `client/`, shader assets, custom models, renderers, HUDs, screens, particles | Port model/buffer/shader/render-event APIs and input/menu behavior. Visually test transparency, lighting, equipment, held items, particles, overlays, GUI scaling, and resource reload. |
| Mixins / access transformers | `TTMixinPlugin.java`, `mixin/`, `src/main/resources/thaumaturge.mixins.json`, `META-INF/` | Audit every target, descriptor, injection point, optional-mod condition, refmap and access transform against 1.20.1. Verify in an actual packaged Forge launch as well as development. |
| Worldgen / dimensions | `content/world/`, `content/eldritch/`, `data/worldgen/`, Lithostitched dependency | Preserve biomes, vegetation, ores, nodes, structures, portals, labyrinth generation, encounters, and return travel. Test new chunks and save/restart. |
| Tests | JUnit Jupiter/Mockito tests use NeoForge's unit-test integration; a GameTest run is configured | Port the existing suite without deleting behavior assertions to obtain a pass. Add focused runtime tests for persistence, recipes, sync, and gameplay; a configured GameTest task alone proves no test coverage. |

Forge references: [capability lifetime, persistence, and sync](https://docs.minecraftforge.net/en/1.20.1/datastorage/capabilities/), [SimpleChannel networking](https://docs.minecraftforge.net/en/1.20.1/networking/simpleimpl/), [custom recipes](https://docs.minecraftforge.net/en/1.20.1/resources/server/recipes/custom/), [GameTests](https://docs.minecraftforge.net/en/1.20.1/misc/gametest/).

## Dependencies and integrations

These are discovery results from publisher-provided Modrinth version metadata filtered for **Forge + 1.20.1**, not tested dependency pins. Verify artifacts, transitive dependencies, APIs, and runtime compatibility during the port. An empty query is not proof that a project has no compatible release elsewhere.

| Integration | Baseline role | Candidate / outstanding investigation |
| --- | --- | --- |
| Lithostitched | Required worldgen library, baseline 1.8.0 NeoForge | [1.4.12-forge-20.1](https://modrinth.com/mod/lithostitched/version/Uqt1Zm1l); audit worldgen API and data compatibility |
| Curios | Required equipment integration, baseline 9.5.1 | [5.14.1+1.20.1](https://modrinth.com/mod/curios/version/IPQlZkz1); port slots, items, renderers, and persistence |
| JEI | Optional recipes, aspect browsing, GUI transfer, internal mixins | [15.74.0.234 beta](https://modrinth.com/mod/jei/version/6E4ps2ER) was the newest returned candidate; choose an appropriate tested version |
| Jade | Optional machine/entity information | [11.13.3+forge](https://modrinth.com/mod/jade/version/xJQHCmWJ) |
| Dynamic Trees | Optional tree placement and bundled tree/resource packs | [1.20.1-1.4.11](https://modrinth.com/mod/vdjF5PL5/version/iGSXZxgo) |
| Distant Horizons | Optional fog/render integration | [3.3.3-1.20.1](https://modrinth.com/mod/distanthorizons/version/6UnEfsRQ) |
| Iris / shaders | Optional rendering integration and mixins | No Iris result for the target filter. [Oculus 1.20.1-1.8.0](https://modrinth.com/mod/oculus/version/iQ1SwGc3) is a candidate equivalent, requiring API and renderer-dependency investigation |
| FTB Library | Compile-only sidebar integration | Target lookup failed; resolve from the publisher's Maven/release sources. Availability remains unverified |
| Sable | Optional ship/sub-level integration spread through space helpers and mixins | No target-filter result; investigate publisher releases/source before deciding feasibility. Preserve normal-world spatial behavior regardless |
| Apothic Enchanting | Enchanting-stat compatibility/data | No target-filter result. [Apotheosis 7.4.8](https://modrinth.com/mod/apotheosis/version/tT3CLYvK) exists for the target and needs semantic/API investigation |

Core content must not disappear because a dependency API differs. Port available target integrations and exercise both their presence and absence. Record any genuinely unavailable integration with primary-source evidence, attempted alternatives, and gameplay impact; obtain an explicit user scope decision before treating a feature omission as accepted completion. Do not silently turn optional integrations into required dependencies.

## Work phases

Each phase can require multiple cycles. End **every implementation/fix cycle with its own commit**, including an honest validation summary and a tracker update. Intermediate commits may be incomplete or failing if accurately documented; no empty commits or artificial pass claims.

1. **Baseline and platform:** refine the content/dependency inventory; preserve an immutable baseline; establish Forge/Java 17 build, metadata, datagen, and runnable test configurations. Implement the silent, non-focusing test-launch setup before any graphical game launch.
2. **Foundations:** convert registrations/events, attachments, item data, capabilities, networking, data maps, and shared serialization. Use actual compilation errors and focused behavior tests to drive work.
3. **Gameplay:** port all feature families, recipes, worldgen, dimensions, AI, inventory transactions, equipment, and progression. Maintain coverage against the baseline inventory.
4. **Client and integrations:** finish models, screens, rendering, particles, shaders, mixins, and supported target integrations. Exercise optional dependencies absent and present.
5. **Runtime validation:** automated tests, datagen, headless server tests, silent background client inspection, progression, persistence, multiplayer, and sustained ticking. Fix discovered defects in separate cycles.
6. **Packaged artifact:** install the reobfuscated distributable and declared dependencies into clean Forge 1.20.1 client/server instances; rerun essential scenarios; finish installation/build instructions and evidence. Do not publish a release as part of this goal.

## Feature and acceptance matrix

Expand these rows into concrete cases as implementation proceeds. Every row needs an outcome, exact tested version/commit, reproducible procedure or command, and evidence. Use **pending**, **in progress**, **pass**, **fail**, or **blocked**, rather than implying untested behavior works.

| Feature family | Required observable coverage | Status |
| --- | --- | --- |
| Build, loader, resources | Clean checkout can resolve dependencies, generate resources, run tests, build a distributable, and load the declared Forge metadata/mixins | In progress — Forge configuration/dependencies and resource processing work; source compilation fails |
| Registrations and content | Inventory every baseline item/block/entity/menu/recipe/custom registry; reconcile target IDs and assets; no silently dropped content | In progress — native handles/factories converted and public fields reconciled; runtime content enumeration pending |
| Research and progression | Survival entry into the mod, Thaumonomicon, scanning, aspect discovery/pools, research table/notes, unlocks, prerequisites, and late-game progression | Pending |
| Aura, nodes, vis | World/chunk aura, node types, interaction/jarring, recharge, transducers, relays, and chunk unload/reload | Pending |
| Alchemy and essentia | Crucible, furnaces/alembics, jars/phials, tubes/suction/filters/valves, reservoirs, thaumatorium, transport, and item/fluid automation | Pending |
| Arcane crafting and infusion | Recipe matching, vis/aspect/research costs, consumed inputs and returned containers, charging, instability, failure/recovery, and completed outputs | Pending |
| Wands, casters, spells | Parts/upgrades, focus editor/equipment/selection, delivery/effects/modifiers, charge/cooldowns, server-authoritative effects, and visuals | Pending |
| Golems and automation | Construction, materials/parts/accessories, seals, task selection, movement, inventory use, farming, combat, ownership, and persistence | Pending |
| Equipment and devices | Armor/trims, tools, shields, Curios, runic shielding, flight/hover, bore, lamps, mirrors, levitators, locks, wards, and remaining registered devices | Pending |
| Flux, taint, warp | Pollution and cleanup, rifts, taint spread/entities/biomes, warp effects, configuration, and long-running updates | Pending |
| Worldgen and encounters | Trees/plants/crystals/ores, biomes, nodes, structures, pechs/cultists/other mobs, eldritch progression, portals, labyrinths, bosses, and return travel | Pending |
| Client presentation | Screens/HUD/keybindings, items/armor/entities/block entities, connected models, shaders, lighting/transparency, particles, textures, translations, and resource reload | Pending |
| Persistence and isolation | Save/restart, chunk unload/reload, item move/copy/drop, death/respawn, logout/rejoin, dimension travel, world changes, and separate players' state | Pending |
| Multiplayer | Actual client connection to dedicated Forge server, two-player state/tracking and interactions, progression, crafting, combat, synchronization, and reconnect | Pending |
| Optional integrations | Tested absence and presence of each supported integration, including recipe transfer/tooltips/tree packs/shader effects as applicable | Pending |
| Stability and package | Sustained representative world activity without port-related crashes, duplication/loss, repeated errors, or severe regressions; actual distribution tested outside development | Pending |

Fresh Forge 1.20.1 worlds and persistence within the port are required. Downgrading an existing Minecraft 1.21.1 world is a separate task and is not assumed safe or part of this goal.

## Silent background game testing

- Use dedicated test profiles/directories and worlds; do not change the user's normal Minecraft profile or system audio.
- Before **every** graphical launch, provision the test profile with `soundCategory_master:0.0`, `narrator:0`, and `fullscreen:false`. Handle first-run accessibility/narrator setup silently. Keep those settings in effect across restarts. Preserve the mod's normal sound content and functionality.
- Launch without activating the game, launcher, or early loading window. On macOS, a shell background job alone does not prevent window activation. Use a genuinely non-activating launch/window mechanism, including Forge early-window handling and development-only GLFW window hints/hooks if necessary; verify the actual focus behavior. Do not claim `open -g` controls a child Java window without evidence.
- Use background-capable inspection/automation and logs; do not repeatedly foreground the client or inject input into the user's active application. Allow the test world to tick while unfocused where needed.
- Prefer headless dedicated-server/GameTest runs for nonvisual checks, and perform the necessary graphical checks in the background. If a particular visual action truly needs foreground access and no safe alternative works, request a single coordinated session; keep that evidence pending until performed.
- Accept the Minecraft server EULA automatically in dedicated test directories, as explicitly authorized by the user.
- Record commands, process IDs, logs, and outcomes; stop only task-owned game/server processes after use. Keep saves, caches, large logs, and screenshots out of commits; retain useful evidence locally with paths in this tracker.

## Completion requirements

All applicable feature rows must have passing evidence, with no unresolved port-related compile, startup, resource, persistence, progression, rendering, or multiplayer defects. A successful build, a zero-test run, or a title-screen screenshot alone is insufficient. Any accepted exception must be an explicit user decision recorded here, never an assumption made to finish early.

Deliver the tested distributable JAR and checksum, exact Minecraft/Forge/Java/dependency versions, installation and reproducible build/test instructions, completed feature matrix, final commit, and a concise account of verification and any explicitly accepted limitations. Keep the goal incomplete while required behavior or evidence is missing. Do not publish, tag a release, or merge into an unrelated branch merely to finish this goal.

## Cycle ledger

| Cycle | Change | Validation / result | Commit |
| --- | --- | --- | --- |
| 000 — planning | Inspected branches, baseline architecture, test inventory, target Forge docs, and dependency metadata; created `1.20.1` and the port documents | Static inspection only; no implementation, build, tests, or game launch | `f5b15d267` |
| 001 — Forge platform | Replaced Prism/NeoForge DSL with ForgeGradle 6.0.54, MixinGradle 0.7.38, Gradle 8.14.3, Forge 47.4.29, official 1.20.1 mappings, Java 17 toolchain; converted metadata/pack/mixin level; target dependency pins, isolated profiles/EULA tasks, and build CI | Resource processing and profile setup pass; actual Java 17 compilation reaches 62 syntax errors in 10 files. Logs under `build/port-evidence/cycle-001/`. No runtime launch | `c7688493e` |
| 002 — Java 17 language | Converted record deconstruction and pattern switches in scanning, essentia tubes, aspect inputs, thaumometer client rendering and debug overlay to Java 17 constructs | `./gradlew compileJava --console=plain` reaches type checking; no prior syntax errors, but the 2,000-error cap is reached on unresolved NeoForge/modern APIs. Log: `build/port-evidence/cycle-002/compile.log`. No behavior tests can run yet | `eafcff3bc` |
| 003 — Forge imports and subscriptions | Converted 134 loader/event/API import paths across production/tests; Forge no-argument entry point/config registration, cancelable events, explicit mod-bus routing in 58 classes, and split lifecycle/gameplay listeners | Main compilation parses converted files but still reaches 2,000 errors in at least 418 files on modern storage, networking, registry, event, rendering and integration APIs. Logs: `build/port-evidence/cycle-003/`. No runtime validation | `36a73a6f8` |
| 004 — Background test windows | Added an isolated Java 17 instrumentation agent, native GLFW smoke test, foreground observer, and per-launch muted/narrator-off profile with actual Forge early-window config | Agent/smoke compilation passes independently of main. GLFW 3.3.1 window stays unfocused after show + explicit focus request (56 samples); monitor records no app changes in 550 samples. Actual Minecraft launch still pending | `85cf0c347` |
| 005 — Binary serialization | Implemented typed binary codec composition, bounded primitive/collection/NBT/registry codecs and 1.20.1 vanilla-value adapters; migrated codec imports/references in 178 files; added focused tests against actual production classes | Codec classes compile on Java 17. 23 of 26 codec tests pass; 3 registry/vanilla tests fail at Forge bootstrap before assertions. Full compiler diagnostic run reports 5,160 errors in 874 files; normal capped compilation still fails. Evidence: `build/port-evidence/cycle-005/` | `04de5171b` |
| 006 — Native Forge registration | Replaced NeoForge deferred-holder/block/item types with native RegistryObject handles, ported item/block factories, and moved 17 custom registries to Forge NewRegistryEvent creation with lazy wrapper access | All 1,599 public registry field names retained. Production factory helper compiles against Java 17/Forge. Full javac inventory falls to 3,744 errors in 850 files; normal capped compile still fails. No registry runtime proof yet | Containing commit |

For each following cycle, record the subsystem, changes, exact commands and outcomes (including failures), evidence paths, remaining issues, next action, and commit reference. A cycle may refer to its containing commit until the next cycle fills in its hash. Keep this document current so work resumes without rediscovering prior results.

## Cycle 001 evidence and next work

- Build invocation on this host: `env JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ./gradlew compileJava processResources --console=plain`. Gradle runs on Java 21; Foojay provisioned Java 17 for compilation. The old daemon criteria forced Java 25 and failed with class version 69; removed those criteria. CI selects Java 17.
- `compile-resolved.log`: configuration, target artifacts, MCP/SRG setup and compiler launch succeed; 62 Java 21 syntax diagnostics in 10 files prevent type checking. This is an incomplete port, not a passing build.
- `./gradlew processResources prepareClientProfile prepareTestServers --console=plain`: passes (same Java environment). Parsed the expanded TOML with Python `tomllib`; Forge mandatory dependency and mod identity are correct. Pack format is 15. Profile options contain zero master volume, narrator off, fullscreen off, accessibility onboarding off, and ticking while unfocused. EULA accepted only in task-owned server directories.
- Current dependency pins: Curios 5.14.1, JEI 15.74.0.234, FTB Library 2001.2.13, and the Modrinth version IDs in `gradle.properties` for Lithostitched/Jade/DH/Dynamic Trees/Oculus. These resolve, but API/runtime compatibility is still unproven. Architectury and Mezz config publisher repositories are included for transitive dependencies.
- No optional-integration source is excluded. Sable project metadata currently declares only 1.21.1 Fabric/NeoForge; source investigation and scope decision remain outstanding. Its source remains present and will produce errors until an appropriate solution is chosen.
- The prior formatter plugin was removed with the Prism build; preserving its Palantir 2.96 formatter on Java 17 requires separate investigation. Existing tests remain configured with JUnit/Mockito; NeoForge test bootstrap still needs replacement before test execution can be claimed. Dynamic Trees development tree-pack layout still needs porting.
- Background focus control is not yet implemented or verified. Do not launch a graphical client until that mechanism is ready.
- Next: convert Java 21 syntax to Java 17, compile again to inventory API errors, then port foundational storage/network/registry APIs.

## Cycle 002 evidence and next work

- Replaced record deconstruction with type patterns and record accessors, and type switches with ordered `instanceof` branches. Preserved null handling for neighbouring tube entities and the object-form input's null rejection.
- Java 17 compiler now parses all main source files and reports unresolved loader/API types. It reaches the configured 2,000 diagnostic limit, so this count is a lower bound, not a full defect inventory. Existing behavior tests remain unexecuted because main compilation fails.
- Next: convert shared Forge loader/event imports and explicit mod-bus subscriptions, registration wrappers, and foundational APIs; then recapture compiler diagnostics.

## Cycle 003 evidence and next work

- Mapped imports against class names in the resolved Forge 47.4.29 and loader/eventbus/merger API artifacts. Converted common Forge classes (including renamed hooks/config/fluid classes) without adding fake API stubs or excluding source. Import changes do not prove method-level API compatibility.
- Forge uses explicit mod and gameplay buses. Added MOD routing for lifecycle/registration-only subscribers; kept gameplay events on FORGE. Mixed subscriptions in keybindings, research scan bootstrap, and attributes are registered separately. Further event API adaptation remains required.
- Replaced NeoForge cancellation marker interfaces with Forge `@Cancelable`, including inherited research-event cancellation. Changed mod construction and config registration to the Forge loading context.
- `compile-dist.log` records the latest real compile failure: diagnostic cap remains at 2,000, chiefly missing component/codec/payload APIs. All runtime behavior and test execution remain unverified.
- Next: implement the isolated non-focusing client-test mechanism, and continue foundational API conversion.

## Cycle 004 evidence and next work

- `./gradlew backgroundAgentJar backgroundSmokeClasses prepareClientProfile --console=plain` passes on Java 21 Gradle / Java 17 toolchains. An initial attempt exposed MixinGradle attaching main compilation/mixins to every Jar task; the independent agent uses a Zip archive with its own JAR manifest so it contains no mod classes or mixin configs. Main source remains fully included in the real build.
- The agent applies `GLFW_FOCUSED=false` and `GLFW_FOCUS_ON_SHOW=false` at `nglfwCreateWindow`, suppresses explicit `glfwFocusWindow` requests, and disables the Cocoa menubar in the test JVM. Inspected transformed bytecode against the exact resolved LWJGL 3.3.1 class (`transformed-glfw.txt`).
- Inspected Forge 47.4.29 loader bytecode: early display is controlled by profile `config/fml.toml` setting `earlyWindowControl`, not the previously assumed `fml.earlyprogresswindow` JVM property. The profile preparation now writes the actual setting.
- Native verification: `./gradlew runBackgroundWindowSmoke --console=plain` passes with a shown window and explicit focus attempt. Its real GLFW focus attributes remained off for 56 samples over three seconds. A separately compiled read-only AppKit monitor observed the foreground PID/application for 550 samples over 12 seconds and recorded no changes. Logs: `build/port-evidence/cycle-004/window-smoke.log` and `focus.log`. Only the test-owned window was created and destroyed; no audio was initialized.
- This verifies the standalone background-window mechanism on this Mac, not a successful Minecraft launch. First actual game launch still requires observation of focus, silence, early display, and rendering. See `porting/testing/README.md` for reproduction and scope.
- Main compilation remains incomplete at the same modern API errors. Next implementation cycle: port reusable serialization/network-codec foundations, then item/attachment storage and Forge networking, using fresh compiler diagnostics to guide work.

## Cycle 005 evidence and next work

- Added mod-owned `serialization/stream/` codecs rather than introducing fake vanilla/NeoForge classes. Their encoders use the 1.20.1 Netty/FriendlyByteBuf/NBT/registry APIs. Typed composition preserves field order; string/list/map/array decoding enforces bounds, and unknown registry IDs fail explicitly. Codec-backed NBT uses a compound wrapper so scalar values can cross the 1.20.1 network NBT API. This is an internal port protocol, not wire compatibility with NeoForge 1.21.1.
- Migrated 178 source/test files to these codecs, including adapters for vanilla identifiers, keys, UUIDs, positions, directions, item stacks, and ingredients. Particle and item-component stream integration still needs conversion.
- `./gradlew compilePortCodecJava --console=plain` passes against resolved Forge 1.20.1 libraries. The `portCodec` source set compiles the actual production files in isolation; main still includes those same files and remains a full failing build. Tests remain in the normal test source tree.
- `./gradlew portCodecTest --console=plain`: **26 tests executed, 23 pass, 3 fail** (`codec-tests-map.log`). Passing coverage includes primitive limits, Minecraft VarInt bytes, malformed/truncated values, Unicode string bounds, optionals, six-field ordering and other composition arities, nested lists, maps and limits, byte arrays, enum IDs, constants, scalar/collection NBT and invalid NBT.
- The 3 registry/vanilla tests require Minecraft bootstrap. Plain JUnit reaches Forge NetworkHooks during Bootstrap and fails because NetworkEvent has not received Forge's event-bus class transformation (`NoSuchMethodException: NetworkEvent.<init>()`). These tests are retained and failing, not skipped or represented as proof of registry behavior. Replace the NeoForge unit-test bootstrap with a real Forge-capable test launch as part of the broader test port.
- `./gradlew compileJava -PportMaxErrors=20000 --console=plain`: javac reports **5,160 errors across 874 files**; `diagnostics.json` records that inventory. The Gradle wrapper then ran out of its small client heap while deserializing the enormous diagnostic exception; terminated only this task-owned wrapper after recording the actual failure. The subsequent normal `./gradlew compileJava --console=plain` terminates normally with the 2,000-error cap (`main-compile-capped.log`). Future uncapped inventories should run with a larger Gradle client heap.
- The codec-specific overload error found in SpellNode's bounded settings map was fixed and covered by a regression test. Broader API conversion remains necessary; this is not a claim that networking or persistence is functional yet.
- Next: continue Forge networking/registry/state conversion and concrete vanilla API adaptations. All gameplay and packaged client/server acceptance remains pending.

## Cycle 006 evidence and next work

- Replaced NeoForge DeferredHolder/DeferredBlock/DeferredItem declarations in 44 production/test files with Forge RegistryObject value handles. Used Forge native DeferredRegister for block/item registries and small lazy factories in `registry/ForgeRegistration.java`; this preserves construction timing and actual namespaced registration IDs. Native handles do not implement ItemLike/Holder, so downstream uses still need type-specific adaptation where required.
- Converted 17 custom registry declarations across golems, mob traits, spells, seals, wand parts and labyrinth definitions to Forge's deferred NewRegistryEvent creation. Preserved every sync=true/sync=false choice. Inspected Forge NewRegistryEvent bytecode to confirm that hasTags creates a vanilla wrapper in the root registry; access is lazy because the wrapper does not exist during static construction. Runtime registration/sync is still unverified.
- Source reconciliation of the registry package retains exactly the same **1,599 public static field names**, with no differences in `registry-fields-before.json`. This is source inventory evidence, not proof of registered content or gameplay parity.
- `./gradlew compilePortCodecJava --console=plain` now also compiles the actual production ForgeRegistration helper against Java 17 and Forge 47.4.29; passes (`factory-compile.log`). No main source was excluded from the full build. Existing codec behavior is unchanged; the 3 bootstrap-dependent test failures remain outstanding.
- Expanded javac run reports **3,744 errors in 850 files**, down from the cycle-005 full inventory of 5,160 errors. Diagnostic records are in `build/port-evidence/cycle-006/diagnostics.json`. That large diagnostic result again exhausts Gradle's exception-reporting heap, even with a 512 MiB wrapper heap, after javac completes. Terminated only its task-owned wrapper. Use bounded compiler batches during conversion; do not infer a working build from a reduced error count.
- The final normal `./gradlew compileJava --console=plain` terminates normally and fails at the 2,000-error cap (`compile-final.log`). Storage components/attachments, payload transport, newer recipe/input APIs, rendering, events and integration APIs remain unported.
- Next: Forge packet transport and explicit state/persistence conversion; continue restoring the genuine Forge test/bootstrap environment. No Minecraft or server launch occurred in cycles 005–006.
