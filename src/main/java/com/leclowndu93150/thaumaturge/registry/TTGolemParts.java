package com.leclowndu93150.thaumaturge.registry;

import static com.leclowndu93150.thaumaturge.registry.TTGolemTraits.traitHolder;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemComponent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.content.golem.parts.GolemArmDart;
import com.leclowndu93150.thaumaturge.content.golem.parts.GolemLegLevitator;
import com.leclowndu93150.thaumaturge.content.golem.parts.RollerLegDust;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTGolemParts {
    public static final DeferredRegister<GolemMaterial> MATERIALS =
            DeferredRegister.create(GolemMaterial.REGISTRY_KEY, TTIds.MODID);
    public static final DeferredRegister<GolemHead> HEADS =
            DeferredRegister.create(GolemHead.REGISTRY_KEY, TTIds.MODID);
    public static final DeferredRegister<GolemArm> ARMS = DeferredRegister.create(GolemArm.REGISTRY_KEY, TTIds.MODID);
    public static final DeferredRegister<GolemLeg> LEGS = DeferredRegister.create(GolemLeg.REGISTRY_KEY, TTIds.MODID);
    public static final DeferredRegister<GolemAddon> ADDONS =
            DeferredRegister.create(GolemAddon.REGISTRY_KEY, TTIds.MODID);

    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<GolemMaterial>> MATERIAL_REGISTRY =
            MATERIALS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<GolemMaterial>().hasTags().disableSync());
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<GolemHead>> HEAD_REGISTRY =
            HEADS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<GolemHead>().hasTags().disableSync());
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<GolemArm>> ARM_REGISTRY =
            ARMS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<GolemArm>().hasTags().disableSync());
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<GolemLeg>> LEG_REGISTRY =
            LEGS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<GolemLeg>().hasTags().disableSync());
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<GolemAddon>> ADDON_REGISTRY =
            ADDONS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<GolemAddon>().hasTags().disableSync());

    public static final RegistryObject<GolemMaterial> WOOD = MATERIALS.register(
            "wood",
            () -> new GolemMaterial(
                    List.of(TTIds.rl("mat_stud_wood")),
                    TTIds.rl("textures/block/base_wood.png"),
                    5059370,
                    6,
                    2,
                    1,
                    stack(TTBlocks.PLANK_GREATWOOD),
                    stack(TTItems.MECHANISM_SIMPLE),
                    List.of(traitHolder(TTGolemTraits.LIGHT))));

    public static final RegistryObject<GolemMaterial> IRON = MATERIALS.register(
            "iron",
            () -> new GolemMaterial(
                    List.of(TTIds.rl("mat_stud_iron")),
                    ResourceLocation.withDefaultNamespace("textures/block/iron_block.png"),
                    16777215,
                    20,
                    8,
                    3,
                    stack(TTItems.PLATE_IRON),
                    stack(TTItems.MECHANISM_SIMPLE),
                    List.of(
                            traitHolder(TTGolemTraits.HEAVY),
                            traitHolder(TTGolemTraits.FIREPROOF),
                            traitHolder(TTGolemTraits.BLASTPROOF))));

    public static final RegistryObject<GolemMaterial> CLAY = MATERIALS.register(
            "clay",
            () -> new GolemMaterial(
                    List.of(TTIds.rl("mat_stud_clay")),
                    ResourceLocation.withDefaultNamespace("textures/block/terracotta.png"),
                    13071447,
                    10,
                    4,
                    2,
                    () -> new ItemStack(Blocks.TERRACOTTA),
                    stack(TTItems.MECHANISM_SIMPLE),
                    List.of(traitHolder(TTGolemTraits.FIREPROOF))));

    public static final RegistryObject<GolemMaterial> BRASS = MATERIALS.register(
            "brass",
            () -> new GolemMaterial(
                    List.of(TTIds.rl("mat_stud_brass")),
                    TTIds.rl("textures/block/metal_brass.png"),
                    15638812,
                    16,
                    6,
                    3,
                    stack(TTItems.PLATE_BRASS),
                    stack(TTItems.MECHANISM_SIMPLE),
                    List.of(traitHolder(TTGolemTraits.LIGHT))));

    public static final RegistryObject<GolemMaterial> THAUMIUM = MATERIALS.register(
            "thaumium",
            () -> new GolemMaterial(
                    List.of(TTIds.rl("mat_stud_thaumium")),
                    TTIds.rl("textures/block/metal_thaumium.png"),
                    5257074,
                    24,
                    10,
                    4,
                    stack(TTItems.PLATE_THAUMIUM),
                    stack(TTItems.MECHANISM_SIMPLE),
                    List.of(
                            traitHolder(TTGolemTraits.HEAVY),
                            traitHolder(TTGolemTraits.FIREPROOF),
                            traitHolder(TTGolemTraits.BLASTPROOF))));

    public static final RegistryObject<GolemMaterial> VOID = MATERIALS.register(
            "void",
            () -> new GolemMaterial(
                    List.of(TTIds.rl("mat_stud_void")),
                    TTIds.rl("textures/block/metal_void.png"),
                    1445161,
                    20,
                    6,
                    4,
                    stack(TTItems.PLATE_VOID),
                    stack(TTItems.MECHANISM_SIMPLE),
                    List.of(traitHolder(TTGolemTraits.REPAIR))));

    public static final RegistryObject<GolemHead> HEAD_BASIC = HEADS.register(
            "basic",
            () -> new GolemHead(
                    List.of(TTIds.rl("mind_clockwork")),
                    partIcon("head_basic"),
                    null,
                    List.of(stackComponent(TTItems.MIND_CLOCKWORK)),
                    null,
                    List.of()));

    public static final RegistryObject<GolemHead> HEAD_SMART = HEADS.register(
            "smart",
            () -> new GolemHead(
                    List.of(TTIds.rl("mind_biothaumic")),
                    partIcon("head_smart"),
                    new GolemPartModel(obj("golem_head_smart"), null, GolemPartModel.AttachPoint.HEAD),
                    List.of(stackComponent(TTItems.MIND_BIOTHAUMIC)),
                    null,
                    List.of(traitHolder(TTGolemTraits.SMART), traitHolder(TTGolemTraits.FRAGILE))));

    public static final RegistryObject<GolemHead> HEAD_SMART_ARMORED = HEADS.register(
            "smart_armored",
            () -> new GolemHead(
                    List.of(TTIds.rl("mind_biothaumic"), TTIds.rl("golem_combat_adv")),
                    partIcon("head_smartarmor"),
                    new GolemPartModel(obj("golem_head_smart_armor"), null, GolemPartModel.AttachPoint.HEAD),
                    List.of(
                            stackComponent(TTItems.MIND_BIOTHAUMIC), stackComponent(TTItems.PLATE_BRASS),
                            GolemComponent.base(), GolemComponent.of(() -> new ItemStack(Blocks.WHITE_WOOL))),
                    null,
                    List.of(traitHolder(TTGolemTraits.SMART))));

    public static final RegistryObject<GolemHead> HEAD_SCOUT = HEADS.register(
            "scout",
            () -> new GolemHead(
                    List.of(TTIds.rl("golem_vision")),
                    partIcon("head_scout"),
                    new GolemPartModel(obj("golem_head_scout"), null, GolemPartModel.AttachPoint.HEAD),
                    List.of(stackComponent(TTItems.MIND_CLOCKWORK), stackComponent(TTItems.MODULE_VISION)),
                    null,
                    List.of(traitHolder(TTGolemTraits.SCOUT), traitHolder(TTGolemTraits.FRAGILE))));

    public static final RegistryObject<GolemHead> HEAD_SMART_SCOUT = HEADS.register(
            "smart_scout",
            () -> new GolemHead(
                    List.of(TTIds.rl("golem_vision"), TTIds.rl("mind_biothaumic")),
                    partIcon("head_smartscout"),
                    new GolemPartModel(obj("golem_head_scout_smart"), null, GolemPartModel.AttachPoint.HEAD),
                    List.of(stackComponent(TTItems.MIND_BIOTHAUMIC), stackComponent(TTItems.MODULE_VISION)),
                    null,
                    List.of(
                            traitHolder(TTGolemTraits.SCOUT),
                            traitHolder(TTGolemTraits.SMART),
                            traitHolder(TTGolemTraits.FRAGILE))));

    public static final RegistryObject<GolemArm> ARMS_BASIC = ARMS.register(
            "basic",
            () -> new GolemArm(
                    List.of(TTIds.rl("mind_clockwork")), partIcon("arms_basic"), null, List.of(), null, List.of()));

    public static final RegistryObject<GolemArm> ARMS_FINE = ARMS.register(
            "fine",
            () -> new GolemArm(
                    List.of(TTIds.rl("mat_stud_brass")),
                    partIcon("arms_fine"),
                    new GolemPartModel(obj("golem_arms_fine"), null, GolemPartModel.AttachPoint.ARMS),
                    List.of(stackComponent(TTItems.MECHANISM_SIMPLE), GolemComponent.base()),
                    null,
                    List.of(traitHolder(TTGolemTraits.DEFT), traitHolder(TTGolemTraits.FRAGILE))));

    public static final RegistryObject<GolemArm> ARMS_CLAWS = ARMS.register(
            "claws",
            () -> new GolemArm(
                    List.of(TTIds.rl("golem_combat_adv")),
                    partIcon("arms_claws"),
                    new GolemPartModel(obj("golem_arms_claws"), null, GolemPartModel.AttachPoint.ARMS),
                    List.of(
                            stackComponent(TTItems.MODULE_AGGRESSION),
                            GolemComponent.of(() -> new ItemStack(Items.SHEARS, 2)),
                            GolemComponent.base()),
                    null,
                    List.of(
                            traitHolder(TTGolemTraits.FIGHTER),
                            traitHolder(TTGolemTraits.CLUMSY),
                            traitHolder(TTGolemTraits.BRUTAL))));

    public static final RegistryObject<GolemArm> ARMS_BREAKERS = ARMS.register(
            "breakers",
            () -> new GolemArm(
                    List.of(TTIds.rl("golem_breaker")),
                    partIcon("arms_breakers"),
                    new GolemPartModel(obj("golem_arms_breakers"), null, GolemPartModel.AttachPoint.ARMS),
                    List.of(
                            GolemComponent.of(() -> new ItemStack(Items.DIAMOND, 2)),
                            GolemComponent.base(),
                            GolemComponent.of(() -> new ItemStack(Blocks.PISTON, 2))),
                    null,
                    List.of(
                            traitHolder(TTGolemTraits.BREAKER),
                            traitHolder(TTGolemTraits.CLUMSY),
                            traitHolder(TTGolemTraits.BRUTAL))));

    public static final RegistryObject<GolemArm> ARMS_DARTS = ARMS.register(
            "darts",
            () -> new GolemArm(
                    List.of(TTIds.rl("golem_combat_adv")),
                    partIcon("arms_darts"),
                    new GolemPartModel(obj("golem_arms_darter"), null, GolemPartModel.AttachPoint.ARMS),
                    List.of(
                            stackComponent(TTItems.MODULE_AGGRESSION),
                            GolemComponent.of(() -> new ItemStack(Blocks.DISPENSER, 2)),
                            GolemComponent.of(() -> new ItemStack(Items.ARROW, 32)),
                            GolemComponent.mechanism()),
                    new GolemArmDart(),
                    List.of(
                            traitHolder(TTGolemTraits.FIGHTER),
                            traitHolder(TTGolemTraits.CLUMSY),
                            traitHolder(TTGolemTraits.RANGED),
                            traitHolder(TTGolemTraits.FRAGILE))));

    public static final RegistryObject<GolemLeg> LEGS_WALKER = LEGS.register(
            "walker",
            () -> new GolemLeg(
                    List.of(TTIds.rl("mind_clockwork")),
                    partIcon("legs_walker"),
                    null,
                    List.of(GolemComponent.base(), GolemComponent.mechanism()),
                    null,
                    List.of()));

    public static final RegistryObject<GolemLeg> LEGS_ROLLER = LEGS.register(
            "roller",
            () -> new GolemLeg(
                    List.of(TTIds.rl("mind_clockwork")),
                    partIcon("legs_roller"),
                    new GolemPartModel(obj("golem_legs_wheel"), null, GolemPartModel.AttachPoint.BODY),
                    List.of(
                            GolemComponent.of(() -> new ItemStack(Items.BOWL, 2)),
                            GolemComponent.of(() -> new ItemStack(Items.LEATHER)),
                            GolemComponent.mechanism()),
                    new RollerLegDust(),
                    List.of(traitHolder(TTGolemTraits.WHEELED))));

    public static final RegistryObject<GolemLeg> LEGS_CLIMBER = LEGS.register(
            "climber",
            () -> new GolemLeg(
                    List.of(TTIds.rl("golem_climber")),
                    partIcon("legs_climber"),
                    new GolemPartModel(
                            obj("golem_legs_climber"),
                            TTIds.rl("textures/block/base_metal.png"),
                            GolemPartModel.AttachPoint.LEGS),
                    List.of(
                            GolemComponent.of(() -> new ItemStack(Items.FLINT, 4)), GolemComponent.base(),
                            GolemComponent.mechanism(), GolemComponent.mechanism()),
                    null,
                    List.of(traitHolder(TTGolemTraits.CLIMBER))));

    public static final RegistryObject<GolemLeg> LEGS_FLYER = LEGS.register(
            "flyer",
            () -> new GolemLeg(
                    List.of(TTIds.rl("golem_flyer")),
                    partIcon("legs_flyer"),
                    new GolemPartModel(obj("golem_legs_floater"), null, GolemPartModel.AttachPoint.BODY),
                    List.of(
                            stackComponent(TTBlocks.LEVITATOR),
                            GolemComponent.of(() -> new ItemStack(TTItems.PLATE_BRASS.get(), 4)),
                            GolemComponent.of(() -> new ItemStack(Items.SLIME_BALL)),
                            GolemComponent.mechanism()),
                    new GolemLegLevitator(),
                    List.of(traitHolder(TTGolemTraits.FLYER), traitHolder(TTGolemTraits.FRAGILE))));

    public static final RegistryObject<GolemAddon> ADDON_NONE = ADDONS.register(
            "none",
            () -> new GolemAddon(
                    List.of(TTIds.rl("mind_clockwork")),
                    TTIds.rl("textures/block/base_metal.png"),
                    null,
                    List.of(),
                    null,
                    List.of()));

    public static final RegistryObject<GolemAddon> ADDON_ARMORED = ADDONS.register(
            "armored",
            () -> GolemAddon.withModels(
                    List.of(TTIds.rl("golem_combat_adv")),
                    partIcon("addon_armored"),
                    List.of(
                            new GolemPartModel(obj("golem_armor"), null, GolemPartModel.AttachPoint.BODY),
                            new GolemPartModel(obj("golem_armor_pauldrons"), null, GolemPartModel.AttachPoint.ARMS)),
                    List.of(GolemComponent.base(), GolemComponent.base(), GolemComponent.base(), GolemComponent.base()),
                    null,
                    List.of(traitHolder(TTGolemTraits.ARMORED), traitHolder(TTGolemTraits.HEAVY))));

    public static final RegistryObject<GolemAddon> ADDON_FIGHTER = ADDONS.register(
            "fighter",
            () -> new GolemAddon(
                    List.of(TTIds.rl("seal_guard")),
                    partIcon("addon_fighter"),
                    null,
                    List.of(stackComponent(TTItems.MODULE_AGGRESSION), GolemComponent.mechanism()),
                    null,
                    List.of(traitHolder(TTGolemTraits.FIGHTER))));

    public static final RegistryObject<GolemAddon> ADDON_HAULER = ADDONS.register(
            "hauler",
            () -> new GolemAddon(
                    List.of(TTIds.rl("mind_clockwork")),
                    partIcon("addon_hauler"),
                    new GolemPartModel(obj("golem_hauler"), null, GolemPartModel.AttachPoint.BODY),
                    List.of(
                            GolemComponent.of(() -> new ItemStack(Items.LEATHER)),
                            GolemComponent.of(() -> new ItemStack(Blocks.CHEST))),
                    null,
                    List.of(traitHolder(TTGolemTraits.HAULER))));

    private TTGolemParts() {}

    private static ResourceLocation partIcon(String name) {
        return TTIds.rl("textures/misc/golem/" + name + ".png");
    }

    private static ResourceLocation obj(String name) {
        return TTIds.rl("models/mesh/" + name + ".ttmesh");
    }

    private static Supplier<ItemStack> stack(Supplier<? extends ItemLike> item) {
        return () -> new ItemStack(item.get());
    }

    private static GolemComponent stackComponent(Supplier<? extends ItemLike> item) {
        return GolemComponent.of(() -> new ItemStack(item.get()));
    }

    public static Registry<GolemMaterial> materials() {
        return ForgeRegistration.vanillaRegistry(MATERIAL_REGISTRY);
    }

    public static Registry<GolemHead> heads() {
        return ForgeRegistration.vanillaRegistry(HEAD_REGISTRY);
    }

    public static Registry<GolemArm> arms() {
        return ForgeRegistration.vanillaRegistry(ARM_REGISTRY);
    }

    public static Registry<GolemLeg> legs() {
        return ForgeRegistration.vanillaRegistry(LEG_REGISTRY);
    }

    public static Registry<GolemAddon> addons() {
        return ForgeRegistration.vanillaRegistry(ADDON_REGISTRY);
    }

    public static void register(IEventBus modBus) {
        MATERIALS.register(modBus);
        HEADS.register(modBus);
        ARMS.register(modBus);
        LEGS.register(modBus);
        ADDONS.register(modBus);
    }
}
