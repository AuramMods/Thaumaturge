# Thaumaturge scripting for Forge 1.20.1

The integration targets Java 17 and the installed Auram versions of Forge (47.4.10), CraftTweaker (14.0.59), and GameStages (15.0.2). Thaumaturge can also run on its own without the scripting mods.

## Processing inventory

This table identifies the server code and data used by each processing path. The scripting and staging columns track what still needs gameplay verification.

| Processing | Native input and execution | Scripting | Stage and verification work |
| --- | --- | --- | --- |
| Arcane Workbench | Shaped and shapeless serializers share `thaumaturge:arcane_workbench`. `ArcaneCraftingTransactions` supplies the menu and external crafting API. The menu also handles ordinary crafting. | `addShaped`, `addShapeless`, native JSON, and primal aspect tag costs. Ordinary recipes use CraftTweaker's crafting manager. | Recipe stage checks run in the transaction, including taking a result already displayed in the menu. |
| Crucible | `thaumaturge:crucible` selects a catalyst and pays from dissolved aspects. The Crucible also converts items into their authored or derived aspects. | `addRecipe`, native JSON, aspect tag costs, and the base aspect data map. | Recipe stages are checked before the Crucible spends its inputs, including through the automation API. The script tests check exact payment and overlapping aspect tags. |
| Thaumatorium | Uses the same Crucible recipe IDs. `BlockEntityThaumatorium` queues recipes, tracks requesters, and pulls essentia before completion. | Uses the Crucible manager and its aspect tag costs. Other machine rules pending. | Script GameTests cover stages, tagged capability transfers, and a queued recipe resumed from saved block data. Dedicated restart remains to be tested. |
| Infusion Matrix | `thaumaturge:infusion`, `thaumaturge:infusion_enchantment`, and `thaumaturge:runic_augment` use `InfusionRecipeMatcher` and the matrix job. Item infusion can output a new stack or modify the existing catalyst. | All three managers accept aspect tag costs. Item infusion also has `addCatalystPatch`. Block data maps configure altar modifiers. | Stage checks run at recipe selection and in-flight work. A tagged job records its concrete payment before starting. |
| Salis Mundus | `SalisMundusItem` selects `thaumaturge:dust_trigger`. The simple, tag, and multiblock serializers share this recipe type. A multiblock references the Blueprint registry. | `addBlock`, `addBlockTag`, `addMultiblock`, and native JSON. Blueprint edits pending. | Stage GameTest covers block and tag transformations, stage revocation, and resumed placement. Verify scripted rotated blueprints. |
| Special crafting | The crafting table uses Salis Mundus mixing, label filtering, and Primordial Pearl repair through Thaumaturge's special serializers. | Verify ordinary crafting manager operations and serializer-backed definitions. | Verify dynamic stack output and remainders with the pack's RecipeStages integration. |
| Alchemical Furnaces | `BlockEntitySmelter` and `BlockEntityAdvancedAlchemicalFurnace` distill item aspects. They use fuel or external power and output essentia. | Edit the base aspect map, item distillation maps, and standard smelter block stats. | Stage checks apply to the input and the proposed aspects before fuel, power, or distillation is spent. |
| Essentia Crystalizer | `BlockEntityEssentiaCrystalizer` buffers one concrete aspect and converts it into a matching crystal or a scripted item output. | The `thaumaturge:crystallization` aspect data map selects costs and results by aspect ID or tag. | Stage checks run at essentia intake and before work or output. The fixture covers exact intake, stage revocation, and resuming a saved batch. |
| Deconstruction Table | `BlockEntityDeconstructionTable` consumes an item and produces a research aspect. | Edit the `thaumaturge:deconstruction` item data map for fixed aspects, points, or probability. | The placer must be eligible for restricted processing. A collector also needs the result's current stages. |
| Infernal Furnace | `BlockEntityInfernalFurnace` uses native smelting recipes with the `InfernalBonus` data map for extra output. | Vanilla smelting manager and `thaumaturge:infernal_bonus` item data map. | Stage rules apply to intake and queued work via the builder or placer. |
| Pattern Crafter | `BlockEntityPatternCrafter` uses the ordinary crafting recipe manager and neighboring storage. | Vanilla crafting manager and `thaumaturge:pattern_crafter` block data map. | Stage rules apply before charging vis or extracting crafting inputs. |
| Golem Press | `BlockEntityGolemBuilder` assembles a configured golem from material and part definitions, then pays Machina. | Edit the `thaumaturge:golem_assembly` data map for materials, parts, and traits. | The script tests exercise tagged material costs, saved Machina, stage revocation, and deployment of the completed build. |
| Golem seals | `SealHandler` places and ticks seals. `SealAccess` checks editors and golem task eligibility together with native ownership and traits. | Stage rules select seal types and the attached blocks. | The fixture uses a block selector to verify placement, editing, and work eligibility before and after a stage grant. |
| Focal Manipulator and spells | `BlockEntityFocalManipulator` validates and inscribes a spell tree. `SpellCasting` and `SpellEngine` control player casts. | Item data maps edit focus tiers and inscription costs. World datapacks define spell parts and affinities. | Stage rules apply to inscription payment, queued work, and new casts through a wand or the spell API. |
| Spa | `BlockEntitySpa` either mixes the tank fluid with an additive or places a fluid from its tank. | The `thaumaturge:spa_mixing` and `thaumaturge:spa_placement` fluid data maps select costs and outputs by fluid or fluid tag. Mixing takes a native item ingredient. | Stage checks run before placement. The script fixture tests blocked outlets, saved contents and owner, and exact costs for both modes. |
| Potion Sprayer | `BlockEntityPotionSprayer` uses `PotionAspects` to derive costs from potion brewing inputs and prepares doses for spraying. | The native brewing manager changes derived costs. The `thaumaturge:potion_sprayer` potion data map sets explicit costs and capacity. | The fixture covers scripted brewing, exact essentia, a saved prepared dose, stage revocation, and delivery to an entity. |
| Condenser, Vis Generator, and Flux Scrubber | These block entities convert aura flux and essentia into Vitium, aura vis into Forge Energy, or physical flux and Aer into Praecantatio. | The `thaumaturge:condenser`, `thaumaturge:vis_generator`, and `thaumaturge:flux_scrubber` block data maps configure the conversions. | Stage checks use the verified placer before new inputs or costs. The fixture verifies exact conversion costs and retained outputs. |
| Wand and scepter assembly | `WandAssemblyPackResources` generates Arcane Workbench recipes from the wand cap and rod registries. | Use the Arcane Workbench manager for the generated recipe IDs. Caps and rods have typed tags. | Verify edits to generated recipes and the normal post-craft assembly hook. |
| Research and scanning | `ResearchManager`, `ResearchNotes`, and the Research Table process research costs and notes. `ScanningManager` processes scanned targets and datapack scan definitions. | World-startup datapack definitions and typed registry tags. | Stage checks run before advancing research, paying note costs, or applying scan effects. |
| Node jarring | `NodeJarRitual` checks research and the glass-and-slab structure, spends six primal vis balances, and converts the node. | `thaumaturge:node_jarring` block data map configures wand payment and modifier degradation. | Stages gate the start, deferred shell removal, and conversion. The fixture checks all six costs and resumes an already-paid job for its original initiator. |
| Other conversions and progression | Audit the Centrifuge, Everfull Urn, Void Siphon, Stabilizer, Pech exchanges, and Eldritch ritual and portal entry for processing or progression hooks. | Pending. | Record what is processed, where server checks belong, and which state persists. |

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

### Arcane Workbench crystal costs

Pass `AspectAmount.tag(tag, amount)` to either Arcane recipe method to offer alternatives. A recipe accepts up to six crystal requirements with up to 64 crystals each. The workbench uses members corresponding to its six primal crystal slots. Removing the last usable member makes a positive tag cost unpayable. Exact costs and overlapping tags share the same inventory during allocation.

```zenscript
import mods.thaumaturge.AspectAmount;

val arcane = <recipetype:thaumaturge:arcane_workbench>;
arcane.addShapeless("tagged_arcane", <item:minecraft:emerald>, 1, [
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:flexible_cost>, 2),
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:arcane_primal>, 3),
    new AspectAmount(<resource:thaumaturge:terra>, 1)
], [<item:minecraft:amethyst_shard>]);
```

The [Arcane fixture](porting/testing/crafttweaker/arcane-tags.zs) uses two tags from the [tag loader](porting/testing/crafttweaker/tags.zs). The complete payment is three Aqua and three Terra. The workbench can use the wand, crystal stocks, or registered workbench vis sources for the full amount allocated to each individual aspect. The Thaumonomicon and JEI cycle the choices in each requirement. For a tagged recipe, JEI transfer fills the crafting grid and you select the crystal stacks.

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

### Essentia Crystalizer

Edit `thaumaturge:crystallization` on the `thaumaturge:aspect` registry. Each entry can set `enabled` (default `true`), `essentia` (default `1`), `progress` (default `200` work units), and `crystals` (default `1`, range `1–64`). The device advances every five ticks while unpowered, with faster progress when it can draw aura vis.

Set `result` to a concrete item stack for a fixed output. Otherwise it produces the specified number of crystals for the batch's actual input aspect. Matching an aspect tag gives every member that rule. Each batch still collects a single aspect, selected by the first unit received. Existing batches keep their original amount and result when rules reload. New batches use the updated rules.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:thaumaturge:aspect>, <resource:thaumaturge:crystallization>, {
    values: {
        "#crafttweaker:probe_aspects": {
            essentia: 3,
            progress: 1,
            result: {id: "minecraft:amethyst_shard", count: 2}
        },
        "thaumaturge:ignis": {enabled: false}
    }
});
```

This [fixture](porting/testing/crafttweaker/crystallizer.zs) converts three Aqua to two amethyst shards using the earlier tag definition. Ignis is disabled. The device uses its normal adjacent inventory and item ejection behavior.

### Spa

Edit `thaumaturge:spa_mixing` or `thaumaturge:spa_placement` on the `minecraft:fluid` registry. The selector is the tank fluid ID or a fluid tag prefixed with `#`. Each mode accepts `enabled` (default `true`), `amount` in millibuckets (default `1000`, range `1–5000`), and `result`, a concrete fluid that can be placed as a source block. Placement uses the tank fluid as its result when omitted. Mixing defaults to purifying fluid.

Mixing also accepts an `additive` item ingredient and a count in `items` (default `1`, range `1–64`). The stock additive is bath salts. Fill the tank before inserting a scripted additive so the slot can use the tank fluid's rule. A work cycle charges inputs once after placing a source. A blocked outlet preserves the tank and additive.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:fluid>, <resource:thaumaturge:spa_mixing>, {
    values: {
        "#minecraft:water": {
            amount: 700,
            additive: {tag: "forge:ingots/copper"},
            items: 2,
            result: "minecraft:lava"
        }
    }
});
ProcessingData.edit(<resource:minecraft:fluid>, <resource:thaumaturge:spa_placement>, {
    values: {"minecraft:water": {amount: 400}}
});
```

The [Spa fixture](porting/testing/crafttweaker/spa.zs) uses 700 millibuckets of water and two copper ingots for a lava source in mixing mode. Placement mode uses 400 millibuckets of water for a water source.

### Pattern Crafter and Infernal Furnace

Add recipes through `craftingTable` and `furnace`. The Pattern Crafter still uses the selected pattern and a single item type, and checks space for both the result and container remainders. Edit the `thaumaturge:pattern_crafter` block data map to set `enabled` (default `true`), `interval` (default 20 ticks), or `vis` (default 1 vis per craft). Set `vis` to zero for free operation.

The Infernal Furnace retains its own cook time and bellows bonuses. Change its additional outputs through the `thaumaturge:infernal_bonus` item data map. Each selector identifies the smelting input, and each bonus specifies an output item, an item list, or an item tag in `items`. `count` defaults to 1 and accepts an integer or vanilla integer provider. `chance` defaults to 1.0 and can range from 0.0 to 1.0. Normal inventory insertion and excess item ejection still apply.

```zenscript
import mods.thaumaturge.ProcessingData;

craftingTable.addShapeless("pattern_milk", <item:minecraft:diamond> * 2, [<item:minecraft:milk_bucket>]);
furnace.addRecipe("infernal_probe", <item:minecraft:emerald> * 2, <item:minecraft:echo_shard>, 0.0f, 20);

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:pattern_crafter>, {
    values: {"thaumaturge:pattern_crafter": {interval: 5, vis: 0.0}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:infernal_bonus>, {
    values: {"minecraft:echo_shard": [{items: "minecraft:gold_nugget", count: 3, chance: 1.0}]}
});
```

The [machine fixture](porting/testing/crafttweaker/machine-crafting.zs) uses these recipes and adds a small test blueprint for the Infernal Furnace.

### Infusion essentia and altar modifiers

All three infusion managers accept `AspectAmount.tag(tag, amount)` in addition to the exact `new AspectAmount(id, amount)` constructor. A tagged requirement accepts any combination of current members. The enchantment or runic level scales the base costs first, and the altar applies its cost multiplier to each resulting requirement. A positive demand with an empty tag blocks the job.

The matrix checks that nearby, accessible essentia sources can satisfy the entire tagged cost. Exact and tagged demands share a single allocation so one unit only counts toward one requirement. The matrix saves that concrete allocation and drains it at the usual cadence. If a source is removed mid-job, reconnect a source of the chosen aspect to resume payment. Recipes made exclusively of exact costs can still start while their jars are empty.

The block data maps `thaumaturge:infusion_pillar_set`, `thaumaturge:infusion_matrix_upgrade`, and `thaumaturge:infusion_pedestal` accept `cycle_time`, `cost`, and `stability`. These fields add to the altar's cycle interval in ticks, its cost multiplier, and the stability gained during cycles. The pillar modifier applies once for a matched set of four. The upgrade modifier applies to each block below the four corner pillars. The pedestal modifier applies to each nearby side pedestal.

```zenscript
import mods.thaumaturge.AspectAmount;
import mods.thaumaturge.ProcessingData;

val infusion = <recipetype:thaumaturge:infusion>;
infusion.addRecipe("tagged_matrix", <item:minecraft:diamond>, <item:minecraft:packed_mud>,
    [<item:minecraft:nether_wart>], [
        AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:flexible_cost>, 4),
        AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:probe_aspects>, 2),
        new AspectAmount(<resource:thaumaturge:terra>, 2)
    ], 0);

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:infusion_matrix_upgrade>, {
    values: {"minecraft:blue_wool": {cost: 0.5, cycle_time: -4}}
});
```

The [infusion fixture](porting/testing/crafttweaker/infusion-tags.zs) uses the aspect tags from the [tag loader fixture](porting/testing/crafttweaker/tags.zs).

### Alchemical Furnaces and Deconstruction Table

Edit `thaumaturge:distillation` or `thaumaturge:advanced_distillation` on the `minecraft:item` registry to select an input by item or item tag. `output` takes a list of concrete aspects and amounts. Omit it to use the normal aspect index. Use `enabled: false` to exclude an input. Each work cycle distills one item. Standard smelters apply their normal efficiency and flux rules to the authored list. Edit the `thaumaturge:smelter_stats` block data map to control efficiency and transfer interval for the three standard smelter tiers. Advanced furnaces spend two heat, one Perditio and one Aqua reserve unit per output point.

Edit `thaumaturge:deconstruction` on the item registry to set `enabled`, a concrete `result` aspect, `points` (default 1), or `chance` (range 0 to 1). A fixed result defaults to guaranteed per item. Without one, the table chooses a primal from the item's normal aspect contents and uses its normal chance unless a chance is specified. An already-completed result keeps its aspect and points across reload, and the normal research discovery bonus and caps apply on collection.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:distillation>, {
    values: {"minecraft:echo_shard": {output: [{aspect: "thaumaturge:ignis", amount: 4}]}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:deconstruction>, {
    values: {"minecraft:echo_shard": {result: "thaumaturge:terra", points: 3}}
});
```

The [distillation fixture](porting/testing/crafttweaker/distillation.zs) also sets the Void Smelter's efficiency to 1.0 and adds an advanced conversion.

### Golem assembly

The `thaumaturge:golem_assembly` data map is available on the `thaumaturge:golem_material`, `thaumaturge:golem_head`, `thaumaturge:golem_arm`, `thaumaturge:golem_leg`, `thaumaturge:golem_addon`, and `thaumaturge:golem_trait` registries. Select entries by ID or by tag. A `components` list replaces only that entry's share of the bill. An empty list removes that share. The press merges identical stacks and computes the default Machina charge as the total item count plus two per resolved trait. Each selected entry's `machina_delta` adjusts that charge, with a minimum final charge of zero.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:thaumaturge:golem_material>, <resource:thaumaturge:golem_assembly>, {
    values: {"#crafttweaker:probe_materials": {
        components: [{id: "minecraft:copper_ingot", count: 3}],
        machina_delta: 2
    }}
});
```

The [golem fixture](porting/testing/crafttweaker/golems.zs) replaces the tagged iron material's base and mechanism with three copper ingots. Selected arms, legs, head and add-on keep their respective costs. The press uses the current bill when the player starts a new job. An in-progress job retains its selected parts, paid items, and Machina balance.

### Focus inscription and casting

Edit `thaumaturge:spell_focus` on `minecraft:item` to set a focus tier's `complexity`, `depth`, `branches`, and `repeats`. Edit `thaumaturge:focus_inscription` on the same registry to set `enabled`, `levels_multiplier`, `crystals_multiplier`, `vis_multiplier`, `interval` in ticks, and aura vis `drain` per interval. The device rounds scaled levels and each aspect's crystal count to the nearest whole unit. The vis multiplier applies to inscription at the table. Spell parts and affinities still determine the cost of a cast.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:spell_focus>, {
    values: {"thaumaturge:focus_3": {complexity: 40, depth: 10, branches: 2, repeats: 2}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:focus_inscription>, {
    values: {"thaumaturge:focus_3": {
        levels_multiplier: 2.0, crystals_multiplier: 0.5, vis_multiplier: 0.5, interval: 2, drain: 7.0
    }}
});
```

The [focus fixture](porting/testing/crafttweaker/focus.zs) uses these values. To edit the spell parts and affinities themselves, put their native definitions under `data/<namespace>/thaumaturge/spell_part/` and `data/<namespace>/thaumaturge/spell_affinity/` in a world datapack. The [projectile definition](porting/testing/crafttweaker/datapack/data/crafttweaker/thaumaturge/spell_part/tagged_projectile.json) adds a part with a native carrier, research requirement, and setting. Close and reopen the world or restart the dedicated server to load changes to those registry entries.

### Potion Sprayer

The sprayer derives essentia costs from the current vanilla brewing table and Forge brewing recipes that expose their ingredients and output. Use CraftTweaker's `brewing` manager to write these recipes. For other custom brewing implementations, specify the cost directly with `thaumaturge:potion_sprayer` on `minecraft:potion`.

```zenscript
import mods.thaumaturge.ProcessingData;

brewing.addRecipe(<item:minecraft:potion>.withTag({Potion: "minecraft:luck"}),
    <item:minecraft:diamond>, <item:minecraft:potion>.withTag({Potion: "minecraft:water"}));

ProcessingData.edit(<resource:minecraft:potion>, <resource:thaumaturge:potion_sprayer>, {
    values: {"minecraft:swiftness": {essentia: [{aspect: "thaumaturge:aqua", amount: 3}], max_charges: 1}}
});
```

Rules select the base potion by ID or potion tag. Omit `essentia` to derive the cost from brewing, or use `essentia: []` for a free dose. An explicit cost uses concrete aspect IDs and totals at most 1000 essentia. `max_charges` sets capacity from one to eight. `enabled: false` suspends preparation and spraying. A partially funded dose keeps its original cost across reloads and saves.

### Aura machines

These rule maps select a block by ID or block tag. Omitted fields retain the usual calculations. Use `enabled: false` to suspend new work. The Condenser still requires a lattice. The Flux Scrubber still uses its native scan range and the Forge configuration's output capacity.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:condenser>, {
    values: {"thaumaturge:condenser": {essentia: 2, interval: 1, flux: 3.0, vitium: 2}}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:vis_generator>, {
    values: {"thaumaturge:vis_generator": {vis: 2.0, energy: 600, max_push: 12}}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:flux_scrubber>, {
    values: {"thaumaturge:flux_scrubber": {power: 3, removal: 2, charges_per_roll: 2, chance: 1.0, essentia: 2}}
});
```

The Condenser uses two buffered essentia and three local aura flux per conversion to yield two Vitium. `interval` is measured in ticks. The generator draws up to two local aura vis for 600 Forge Energy, with proportional output if less vis is available. It sends up to 12 FE per tick from its buffer. The scrubber spends three Aer per successful action and removes up to two units of physical flux. Each removed unit adds a charge. Here every two charges recover two Praecantatio, up to the storage capacity. Omit `charges_per_roll`, `chance`, or `essentia` to use the corresponding Forge configuration.

### Node jarring

The `thaumaturge:node_jarring` data map selects the original node block by ID or block tag. `vis_per_primal` is the base wand cost for each of the six primal aspects. Equipment and wand discounts apply afterward. `degrade_chance` controls whether the node's modifier degrades and accepts a value from zero to one. The usual research, shell, and wand requirements still apply.

```zenscript
import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:node_jarring>, {
    values: {"thaumaturge:node": {vis_per_primal: 2, degrade_chance: 0.0}}
});
```

Use `enabled: false` to prevent new rituals. A paid ritual retains its saved modifier rule, timer, and initiating player.

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
| `thaumaturge:essentia_crystalizer` | The concrete input aspect ID. Supports tags and entries in `thaumaturge:aspect`. |
| `thaumaturge:spa` | `thaumaturge:spa_mixing` or `thaumaturge:spa_placement`. Supports the actual tank fluid in `minecraft:fluid` and the mixing additive in `minecraft:item`. |
| `thaumaturge:pattern_crafter` | Ordinary crafting recipe IDs and the actual input in `minecraft:item`. |
| `thaumaturge:infernal_furnace` | Ordinary smelting recipe IDs and the actual input in `minecraft:item`. |
| `thaumaturge:alchemical_furnace` and `thaumaturge:advanced_alchemical_furnace` | The input item ID. Supports `minecraft:item` input and `thaumaturge:aspect` output entries and tags. |
| `thaumaturge:deconstruction_table` | The input item ID. Supports `minecraft:item` input and `thaumaturge:aspect` result entries and tags. |
| `thaumaturge:golem_assembly` and `thaumaturge:golem_deployment` | The design's material ID. Supports the selected material, head, arms, legs, add-on, and effective traits as registry entries and tags. |
| `thaumaturge:golem_seal` | The seal type ID. Supports `thaumaturge:seal` and the attached block in `minecraft:block`. |
| `thaumaturge:focus_inscription` and `thaumaturge:spell_cast` | The focus item ID, when present. Supports the focus in `minecraft:item`, and the spell tree's entries in `thaumaturge:spell_part`, `thaumaturge:aspect`, and `thaumaturge:spell_affinity`. |
| `thaumaturge:potion_sprayer` | The base potion ID. Supports selectors for the potion in `minecraft:potion` and the received bottle in `minecraft:item`. |
| `thaumaturge:condenser`, `thaumaturge:vis_generator`, and `thaumaturge:flux_scrubber` | The device block ID and its entry or tag in `minecraft:block`. The Condenser also supports Vitium as an aspect selector. The Flux Scrubber supports Aer and Praecantatio. |
| `thaumaturge:node_jarring` | The original node block ID. Supports selectors for the source node and jar blocks in `minecraft:block`, and the six primal aspects in `thaumaturge:aspect`. |

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

The Crystalizer records the player who placed it. For restricted aspects the owner must be online and eligible for intake and progress. When the owner loses the stage, the device retains its input and work across saves. To use a restricted recipe in an older Crystalizer without a recorded owner, re-place an empty device as the intended owner.

The Spa records the player who placed it and checks that owner's current stages before placing a restricted source. Players can fill the tank and load additives while placement is suspended. Re-place an empty legacy Spa to record its owner.

The Pattern Crafter records its placer. The Infernal Furnace records its Salis Mundus builder or its placer when placed directly. Restricted work uses that player's current stages. The Infernal Furnace keeps its queued inputs and pauses its timer when no queued input is eligible. Re-place or reassemble empty legacy devices to record their owner.

The Alchemical Furnaces and Deconstruction Table record their placer or Salis Mundus builder. Revoking a stage pauses restricted work and retains its inputs and paid reserves. The smelters can transfer essentia from completed jobs. A Deconstruction Table retains a completed result until a player with its stages collects it, even if that player and the placer are different people.

The Golem Press checks the requester before collecting items. The requester's stages govern Machina intake, progress, and delivery. A seal checks the actor placing or editing it and uses the current seal owner's stages for golem work. A suspended seal remains placed and resumes accepting eligible tasks when its owner returns with the required stages. Native trait and ownership checks still apply.

The Focal Manipulator saves the original requester and the paid job's aura balance and timing. It pauses a restricted inscription while the requester is offline or lacks a stage. The casting family applies to new player casts and to each channeled pulse. Existing projectiles and other effects from an authorized cast continue their native behavior.

The Potion Sprayer records its placer and checks that owner's stages before drawing essentia or spraying. Changing the owner's stages preserves partial payment and prepared doses. Once the owner is eligible again, a new rising redstone edge can release an existing dose.

The [aura machine fixture](porting/testing/crafttweaker/aura-machines.zs) stages the Condenser, Vis Generator, and Flux Scrubber. These machines retain their recorded owners and buffers when saved. Suspending new work leaves completed essentia available for transfer and lets the Vis Generator send energy it already produced.

The [node jarring fixture](porting/testing/crafttweaker/node-jarring.zs) gates the ritual on the resulting jar block. The paid ritual and its remaining shell swaps pause while the initiating player is offline or loses the required stage. They resume after that player becomes eligible and finish without a second payment. A legacy ritual already in progress keeps the original authorization.

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
