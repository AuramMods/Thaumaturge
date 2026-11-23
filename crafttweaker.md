# Thaumaturge scripting for Forge 1.20.1

The integration targets Java 17 and the installed Auram versions of Forge (47.4.10), CraftTweaker (14.0.59), and GameStages (15.0.2). Thaumaturge can also run on its own without the scripting mods.

## Processing inventory

This table identifies the server code and data used by each processing path. The scripting and staging columns track what still needs gameplay verification.

| Processing | Native input and execution | Scripting | Stage and verification work |
| --- | --- | --- | --- |
| Arcane Workbench | Shaped and shapeless serializers share `thaumaturge:arcane_workbench`. `ArcaneCraftingTransactions` supplies the menu and external crafting API. The menu also handles ordinary crafting. | `addShaped`, `addShapeless`, and native JSON. Ordinary recipes use CraftTweaker's crafting manager. | Recipe stage checks run in the transaction. Verify result pickup, shift crafting, and external transactions with different permissions. |
| Crucible | `thaumaturge:crucible` selects a catalyst and pays from dissolved aspects. The Crucible also converts items into their authored or derived aspects. | `addRecipe`, native JSON, aspect tag costs, and the base aspect data map. | Recipe stages are checked before the Crucible spends its inputs, including through the automation API. The script tests check exact payment and overlapping aspect tags. |
| Thaumatorium | Uses the same Crucible recipe IDs. `BlockEntityThaumatorium` queues recipes, tracks requesters, and pulls essentia before completion. | Uses the Crucible manager and its aspect tag costs. Other machine rules pending. | Script GameTests cover stages, tagged capability transfers, and a queued recipe resumed from saved block data. Dedicated restart remains to be tested. |
| Infusion Matrix | `thaumaturge:infusion`, `thaumaturge:infusion_enchantment`, and `thaumaturge:runic_augment` use `InfusionRecipeMatcher` and the matrix job. Item infusion can output a new stack or modify the existing catalyst. | Each manager has `addRecipe` and native JSON. Item infusion also has `addCatalystPatch`. Infusion modifier map edits pending. | Stage checks run at recipe selection and in-flight work. Verify each variant, costs, continuation, and dynamic output state. |
| Salis Mundus | `SalisMundusItem` selects `thaumaturge:dust_trigger`. The simple, tag, and multiblock serializers share this recipe type. A multiblock references the Blueprint registry. | `addBlock`, `addBlockTag`, `addMultiblock`, and native JSON. Blueprint edits pending. | Stage GameTest covers block and tag transformations, stage revocation, and resumed placement. Verify scripted rotated blueprints. |
| Special crafting | The crafting table uses Salis Mundus mixing, label filtering, and Primordial Pearl repair through Thaumaturge's special serializers. | Verify ordinary crafting manager operations and serializer-backed definitions. | Verify dynamic stack output and remainders with the pack's RecipeStages integration. |
| Alchemical Furnaces | `BlockEntitySmelter` and `BlockEntityAdvancedAlchemicalFurnace` distill item aspects. They use fuel or external power and output essentia. | Aspect maps and machine rules pending. | Verify tag-driven input changes, capacity, output transfer, and the actor policy. |
| Essentia Crystalizer | `BlockEntityEssentiaCrystalizer` converts a measured amount of one aspect into its crystal. | Aspect selection and conversion rule pending. | Verify the accepted aspect, exact cost, and full output behavior. |
| Deconstruction Table | `BlockEntityDeconstructionTable` consumes an item and produces a research aspect. | Aspect mappings and any conversion override pending. | Verify consumption, the claimed result, and the research update. |
| Infernal Furnace | `BlockEntityInfernalFurnace` uses native smelting recipes with the `InfernalBonus` data map for extra output. | Vanilla smelting manager and bonus data map. Script edits pending verification. | Verify altered smelting and bonus output, including queued input and full output. |
| Pattern Crafter | `BlockEntityPatternCrafter` uses the ordinary crafting recipe manager and neighboring storage. | Vanilla crafting manager. Machine rules pending. | Verify changed recipes, container remainders, and denied automation. |
| Golem Press | `BlockEntityGolemBuilder` assembles a configured golem from material and part definitions, then pays Machina. | Golem part and trait selectors, costs, and press rules pending. | Verify item costs, essentia costs, saved work, stage checks, and exact final golem state. |
| Focal Manipulator | `BlockEntityFocalManipulator` validates a spell tree and charges vis against focus tiers, spell parts, and affinities. | Focus data and inscription rules pending. | Verify setup through the menu packet, an exact charge, saved completion, and casting restrictions. |
| Spa | `BlockEntitySpa` places a fluid source and combines bath salts with water for purifying fluid. | Typed fluid and item rules pending. | Verify tank and item counts, source placement, and cancellation. |
| Potion Sprayer | `BlockEntityPotionSprayer` uses `PotionAspects` to derive costs from potion brewing inputs and prepares doses for spraying. | Brewing manager and potion cost rules pending. | Verify altered inputs and the cost of a delivered dose. |
| Wand and scepter assembly | `WandAssemblyPackResources` generates Arcane Workbench recipes from the wand cap and rod registries. | Use the Arcane Workbench manager for the generated recipe IDs. Caps and rods have typed tags. | Verify edits to generated recipes and the normal post-craft assembly hook. |
| Research and scanning | `ResearchManager`, `ResearchNotes`, and the Research Table process research costs and notes. `ScanningManager` processes scanned targets and datapack scan definitions. | World-startup datapack definitions and typed registry tags. | Stage checks run before advancing research, paying note costs, or applying scan effects. |
| Other conversions and progression | Audit the Condenser, Flux Scrubber, Vis Generator, node jarring, Pech exchanges, crafting from research data, spell casting, and Eldritch ritual and portal entry for processing or progression hooks. | Pending. | Record what is processed, where server checks belong, and which state persists. |

## Recipe manager methods

Each manager can look up recipes by ID or remove them with CraftTweaker's standard `removeByName` method. `definition(id)` returns the recipe's JSON. `addJsonRecipe(name, data)` accepts a ZenScript data map, and `addJson(name, json)` accepts a JSON string. `replaceRecipe(id, data)` replaces a recipe of the same type after validating the replacement. Arcane Workbench and dust-trigger JSON must include `type` because each manager has more than one serializer.

All typed methods accept a final optional `ResearchRequirement`. Use `ResearchRequirement.entry(id)` for a completed entry, `ResearchRequirement.stage(id, stage)` for a numbered stage, or call `.negated()` on either condition. Use `new AspectAmount(id, amount)` for each aspect cost. The shapes in the table use the same argument order as the methods.

| Recipe manager | Typed methods and arguments |
| --- | --- |
| `<recipetype:thaumaturge:arcane_workbench>` | `addShaped(name, output, vis, crystals, pattern, key, research?)` · `addShapeless(name, output, vis, crystals, ingredients, research?)` |
| `<recipetype:thaumaturge:crucible>` | `addRecipe(name, output, catalyst, aspects, research?)` |
| `<recipetype:thaumaturge:infusion>` | `addRecipe(name, output, catalyst, components, aspects, instability, research?)` · `addCatalystPatch(name, patch, catalyst, components, aspects, instability, research?)` |
| `<recipetype:thaumaturge:infusion_enchantment>` | `addRecipe(name, enchantment, displayCatalyst, components, aspects, research?)` |
| `<recipetype:thaumaturge:runic_augment>` | `addRecipe(name, displayCatalyst, components, perLevel, aspects, research?)` |
| `<recipetype:thaumaturge:dust_trigger>` | `addBlock(name, output, block, research?)` · `addBlockTag(name, output, tag, research?)` · `addMultiblock(name, output, blueprintId, research?)` |

The [processing fixture](porting/testing/crafttweaker/processing.zs) shows each typed method. In the shaped recipe key, cast item stacks to `IIngredient` so ZenCode resolves the map's type. For a recipe with an input-dependent result, use its ID when removing it.

### Recipe dumps and selective edits

Run `/ct recipes manager thaumaturge:arcane_workbench` in game and open the CraftTweaker log for the dumped definitions. The same command accepts the other recipe type IDs in the table. The dump calls `addJson` with the full ID and its serializer's JSON.

CraftTweaker's `Replacer` can edit the item ingredients and fixed results using `<recipecomponent:crafttweaker:input/ingredients>` and `<recipecomponent:crafttweaker:output/items>`. The [replacement fixture](porting/testing/crafttweaker/replacer.zs) changes one material in a shaped recipe while preserving its pattern, result stack, vis and crystal costs, and research requirement. Replacer assigns its usual generated ID. Use `replaceRecipe` when a book entry, stage rule, or other data needs to keep referencing the original ID.

### Vanilla and special crafting recipes

Use `craftingTable` for ordinary crafting, `furnace` for smelting, `blastFurnace` for blasting, `smithing` for smithing, and `stoneCutter` for stonecutting. Thaumaturge's Pattern Crafter reads ordinary crafting recipes. Its Infernal Furnace reads ordinary smelting recipes.

The native special crafting recipes have the IDs `thaumaturge:salis_mundus`, `thaumaturge:label_filter`, and `thaumaturge:primordial_pearl_repair`. Use `craftingTable.removeByName` to remove them. Use `craftingTable.addJsonRecipe` with the corresponding serializer ID as `type` to register the existing processing behavior under a new ID. The [special crafting fixture](porting/testing/crafttweaker/special-crafting.zs) demonstrates both operations.

## Custom registry tags

Use `<tag:thaumaturge/aspect:namespace:tag_name>` for aspect tags. To edit membership early, put this in a separate `.zs` file.

```zenscript
#loader tags

val aspects = <tag:thaumaturge/aspect:crafttweaker:probe_aspects>;
aspects.addId(<resource:thaumaturge:aqua>, <resource:thaumaturge:terra>);
aspects.removeId(<resource:thaumaturge:terra>);
```

In the default loader, the same bracket returns `KnownTag<Aspect>`. Use `.contains(id)` to check membership or `.idElements` to list the member IDs. The [tag fixture](porting/testing/crafttweaker/tag-lookup.zs) shows typed lookups for aspects and golem materials. The custom registry folders include `aspect`, `blueprint`, `golem_material`, `golem_head`, `golem_arm`, `golem_leg`, `golem_addon`, `golem_trait`, `seal`, `spell_part`, `spell_affinity`, `research_entry`, `research_category`, `scan`, `wand_cap`, `wand_rod`, `labyrinth`, `labyrinth_room`, and `obelisk_site`. Prefix these folders with `thaumaturge/` in tag brackets.

### Paying the Crucible or Thaumatorium with tags

In the default loader, use `AspectAmount.tag(tag as KnownTag<Aspect>, amount as int)` in a Crucible recipe. Any combination of current tag members can cover the requested amount. The server allocates all requirements together, so overlapping tags and exact costs can share an aspect supply while each unit is spent only once. An empty tag with a positive cost prevents crafting. The Thaumatorium keeps its buffered essentia and stops accepting new payment for that recipe until the tag has members.

```zenscript
import mods.thaumaturge.AspectAmount;

val crucible = <recipetype:thaumaturge:crucible>;
crucible.addRecipe("tagged_cost", <item:minecraft:diamond>, <item:minecraft:iron_nugget>, [
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:flexible_cost>, 2),
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:probe_aspects>, 3),
    new AspectAmount(<resource:thaumaturge:terra>, 1)
]);
```

Use the tag loader to define the two tags. In the [tag fixture](porting/testing/crafttweaker/tags.zs), `flexible_cost` contains Aqua and Terra, and the script removes Terra from `probe_aspects`. The recipe can then use three Aqua and three Terra. Five Terra and two Aqua cannot satisfy it. Run `/reload` after changing membership. The Crucible still processes uncraftable catalysts through its normal dissolution behavior. A flexible cost contributes no fixed aspect to derived item aspects. Use the base aspect data map to specify those item aspects explicitly.

For JSON, each cost entry takes either `{"aspect": "thaumaturge:terra", "amount": 1}` or `{"tag": "crafttweaker:flexible_cost", "amount": 2}`. Aspects stored in jars, items, and other concrete outputs use a specific aspect ID. The new cost encoding requires the same Thaumaturge build on the client and the server.

## Processing data maps

`ProcessingData.edit(registry, map, definition)` adds a layer to an existing native data map. Pass the same `values` and `remove` fields as a datapack data-map file. A selector can be an entry ID or a tag ID prefixed with `#`. For one entry, wrap its value with `{replace: true, value: ...}` to replace the existing entry. Use top-level `replace: true` to start with an empty map before processing that layer.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:base_aspects>, {
    values: {
        "minecraft:dirt": {
            replace: true,
            value: [{aspect: "thaumaturge:terra", amount: 71}]
        }
    }
});
```

Run data-map edits in the default loader. The server reads the values through the native codec and applies membership after tags bind. This example changes the base aspect value read by the processing machines. The isolated fixture verifies the edited value in the server's written aspect index after startup and datapack reload.

## GameStages

Register requirements in the default loader. GameStages reads each online player's current stages on the server. Rules apply together when more than one selector matches. Use `StageRequirement.allOf(string[])` to require every listed stage and `StageRequirement.anyOf(string[])` to accept any listed stage. Each array needs at least one nonempty GameStages name.

| Method | Selector |
| --- | --- |
| `Stages.requireFamily(family, requirement)` | Every operation in a family. |
| `Stages.requireId(family, id, requirement)` | One operation ID, such as a full recipe ID. |
| `Stages.requireEntry(family, registry, id, requirement)` | Operations using a specified entry in a registry supported by that family. |
| `Stages.requireTag(family, tag, requirement)` | Operations using an entry in a supported registry tag. Pass the native CraftTweaker tag bracket. |

| Family | Operation IDs and registry selectors |
| --- | --- |
| `thaumaturge:arcane_workbench` | Arcane recipe IDs. |
| `thaumaturge:crucible` | Crucible recipe IDs at either station. |
| `thaumaturge:thaumatorium` | Crucible recipe IDs at the Thaumatorium. These requirements apply alongside the Crucible family's rules. |
| `thaumaturge:infusion` | Item infusion, infusion enchantment, and Runic Augment recipe IDs. |
| `thaumaturge:dust_trigger` | Salis Mundus recipe IDs, clicked blocks in `minecraft:block`, and multiblock blueprints in `thaumaturge:blueprint`. |
| `thaumaturge:research` | Progression IDs, including authored research entries and discovery flags. Supports `thaumaturge:research_entry` and `thaumaturge:research_category` selectors. |
| `thaumaturge:scan` | Datapack scan IDs, or the research IDs awarded by code-driven scans. Supports `thaumaturge:scan` and `thaumaturge:research_entry` selectors, plus the directly scanned target in `minecraft:block`, `minecraft:item`, or `minecraft:entity_type`. |

```zenscript
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

Stages.requireFamily(<resource:thaumaturge:thaumatorium>, StageRequirement.allOf(["automate_alchemy"]));
Stages.requireEntry(<resource:thaumaturge:dust_trigger>, <resource:minecraft:block>, <resource:minecraft:iron_block>,
    StageRequirement.anyOf(["transform_iron", "craft_initiate"]));
Stages.requireTag(<resource:thaumaturge:dust_trigger>, <tag:blocks:minecraft:gold_ores>,
    StageRequirement.allOf(["transform_gold"]));
```

The [stage fixture](porting/testing/crafttweaker/stages.zs) adds a recipe-specific rule and a custom blueprint tag rule. Edit the rules and run `/reload` to apply the new scripts. Configured restrictions require GameStages and an eligible online actor. A server without any stage rules follows normal recipe behavior.

The Thaumatorium checks the player who queued each recipe before drawing essentia, accepting an essentia transfer, or completing the recipe. If that player logs off or loses a required stage, the recipe and buffered essentia are retained. Queue ownership and buffered essentia survive saving and loading. For an older queue saved without its player, remove and requeue its restricted recipe with an eligible player. Infusion jobs and deferred Salis Mundus changes check their saved initiator before continuing. Grant the stage again to let a suspended job resume its existing progress.

Research stage rules apply to unlocking, advancing, completing, and working on an entry's notes. Existing knowledge remains available. Revoking a stage suspends new progress and preserves research and notes already earned. A scan rule can gate a whole target or an individual matching scan definition. The direct scan API uses its research key when no target or definition is available.

`./gradlew runGameTestServer -PportScriptIntegrations` exercises the recipe stages with the integration fixtures. `porting/testing/crafttweaker/verify.py` verifies the same scripts load with the packaged Forge build and Auram's CraftTweaker, GameStages, RecipeStages, and ItemStages JARs.

### Editing research and scan definitions

Put research entries in `data/<namespace>/thaumaturge/research_entry/<id>.json` and scan definitions in `data/<namespace>/thaumaturge/scan/<id>.json` in a world datapack. Forge 1.20.1 loads both registries when opening the world. Restart the dedicated server or close and reopen the singleplayer world after editing definitions. Tag membership and stage conditions can be changed with `/reload`.

The [research fixture](porting/testing/crafttweaker/datapack/data/crafttweaker/thaumaturge/research_entry/paid_stage.json) creates an entry in Basics that costs one gold nugget. The [scan fixture](porting/testing/crafttweaker/datapack/data/crafttweaker/thaumaturge/scan/honeycomb.json) registers a Honeycomb scan that grants `crafttweaker:scanned_honeycomb`. A scan offers its own registry ID for selectors and a `key` for the knowledge it grants. The [tag loader](porting/testing/crafttweaker/tags.zs) puts both definitions into custom tags and the [default loader](porting/testing/crafttweaker/stages.zs) requires stages for those tags.

## Crucible and Thaumatorium recipes

Use the same manager for both stations. Put the following in a `.zs` file under `scripts/` on the server.

```zenscript
import mods.thaumaturge.AspectAmount;

val crucible = <recipetype:thaumaturge:crucible>;
crucible.addRecipe("clay_from_sand", <item:minecraft:clay_ball> * 2, <item:minecraft:sand>, [
    new AspectAmount(<resource:thaumaturge:aqua>, 3),
    new AspectAmount(<resource:thaumaturge:terra>, 5)
]);
```

An unqualified recipe name is placed in the `crafttweaker` namespace. Use the full ID when inspecting, removing, or replacing an existing recipe. `definition(id)` returns the native JSON definition, including the research requirement if present. `replaceRecipe(id, data)` replaces an existing recipe under its original ID using the same JSON fields.

```zenscript
val crucible = <recipetype:thaumaturge:crucible>;
println(crucible.definition(<resource:thaumaturge:crucible/nitor_yellow>));
crucible.replaceRecipe(<resource:thaumaturge:crucible/nitor_yellow>, {
    catalyst: {tag: "forge:dusts/glowstone"},
    aspects: [
        {aspect: "thaumaturge:potentia", amount: 8},
        {aspect: "thaumaturge:ignis", amount: 8},
        {aspect: "thaumaturge:lux", amount: 8}
    ],
    result: {id: "thaumaturge:nitor_yellow", count: 1},
    research: {entry: "thaumaturge:unlock_alchemy", stage: 2}
});
```
