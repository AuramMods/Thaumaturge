# Thaumaturge scripting for Forge 1.20.1

The integration targets Java 17 and the installed Auram versions of Forge (47.4.10), CraftTweaker (14.0.59), and GameStages (15.0.2). Thaumaturge can also run on its own without the scripting mods.

## Processing inventory

This table identifies the server code and data used by each processing path. The scripting and staging columns track what still needs gameplay verification.

| Processing | Native input and execution | Scripting | Stage and verification work |
| --- | --- | --- | --- |
| Arcane Workbench | Shaped and shapeless serializers share `thaumaturge:arcane_workbench`. `ArcaneCraftingTransactions` supplies the menu and external crafting API. The menu also handles ordinary crafting. | Typed managers and native JSON pending. Ordinary recipes use CraftTweaker's crafting manager. | Check player preview, result pickup, shift crafting, and external transactions with equal recipe inputs and different permissions. |
| Crucible | `thaumaturge:crucible` selects a catalyst and pays from dissolved aspects. The Crucible also converts items into their authored or derived aspects. | Typed recipe manager and native JSON under verification. Aspect mapping pending. | Verify exact catalyst, aspect payment, output, and denial behavior. |
| Thaumatorium | Uses the same Crucible recipe IDs. `BlockEntityThaumatorium` queues recipes, tracks requesters, and pulls essentia before completion. | Uses the Crucible manager. Machine rules pending. | Verify queue selection, owner eligibility, stage changes, and saved payment across reload and restart. |
| Infusion Matrix | `thaumaturge:infusion`, `thaumaturge:infusion_enchantment`, and `thaumaturge:runic_augment` use `InfusionRecipeMatcher` and the matrix job. Item infusion can output a new stack or modify the existing catalyst. | Typed managers, native JSON, and infusion modifier maps pending. | Verify each variant, pedestal counts, instability, per-level costs, continuation, and dynamic output state. |
| Salis Mundus | `SalisMundusItem` selects `thaumaturge:dust_trigger`. The simple, tag, and multiblock serializers share this recipe type. A multiblock references the Blueprint registry. | Typed managers, blueprint data, and native JSON pending. | Verify block, tag and rotated blueprint transformations and the delayed replacement queue. |
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
| Other conversions and progression | Audit the Condenser, Flux Scrubber, Vis Generator, node jarring, Pech exchanges, crafting from research data, spell casting, and Eldritch ritual and portal entry for processing or progression hooks. | Pending. | Record what is processed, where server checks belong, and which state persists. |

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
