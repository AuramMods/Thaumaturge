craftingTable.removeByName("thaumaturge:salis_mundus", "thaumaturge:label_filter", "thaumaturge:primordial_pearl_repair");
craftingTable.addJsonRecipe("probe_salis_mundus", {type: "thaumaturge:salis_mundus"});
craftingTable.addJsonRecipe("probe_label_filter", {type: "thaumaturge:label_filter"});
craftingTable.addJsonRecipe("probe_primordial_pearl_repair", {type: "thaumaturge:primordial_pearl_repair"});

println("TT_SPECIAL_REMOVED=" + (craftingTable.getRecipeByName("thaumaturge:label_filter") == null));
println("TT_SPECIAL_SALIS=" + (craftingTable.getRecipeByName("crafttweaker:probe_salis_mundus") != null));
println("TT_SPECIAL_LABEL=" + (craftingTable.getRecipeByName("crafttweaker:probe_label_filter") != null));
println("TT_SPECIAL_PEARL=" + (craftingTable.getRecipeByName("crafttweaker:probe_primordial_pearl_repair") != null));
