import mods.thaumaturge.ProcessingData;
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:pech_offer>, {
    values: {"minecraft:amethyst_shard": {value: 3}}
});
ProcessingData.edit(<resource:thaumaturge:pech_trade>, <resource:thaumaturge:pech_trades>, {
    values: {"#crafttweaker:probe_pech_tables": {
        trades: [{tier: 1, result: {id: "minecraft:prismarine_shard", count: 2}}]
    }}
});

val tables = <tag:thaumaturge/pech_trade:crafttweaker:probe_pech_tables>;
println("TT_TAG_PECH=" + tables.contains(<resource:thaumaturge:forager>));
Stages.requireTag(<resource:thaumaturge:pech_trade>, tables, StageRequirement.allOf(["barter_with_pech"]));
