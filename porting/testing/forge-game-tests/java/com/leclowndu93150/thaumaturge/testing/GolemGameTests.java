package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemDart;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.MenuGolemBuilder;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.network.ServerboundGolemPressPayload;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class GolemGameTests {
    private static final BlockPos PRESS = new BlockPos(4, 2, 4);
    private static final BlockPos ANVIL = PRESS.west().south();
    private static final BlockPos CHEST = PRESS.east();
    private static final BlockPos JAR = PRESS.below();
    private static final BlockPos DEPLOY = PRESS.south(2);
    private static final int OUTPUT = BlockEntityGolemBuilder.SLOT_OUTPUT;
    private static final int MACHINA_COST = 14;

    private GolemGameTests() {}

    @GameTest(template = "golem_room", timeoutTicks = 160)
    public static void fezRepairsBeforeBareGolem(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        level.addNewPlayer(player);
        EntityThaumaturgeGolem equipped;
        try {
            equipped = deployDefaultGolem(test, player);
            fit(test, player, equipped, new ItemStack(TTItems.GOLEM_FEZ.get()));
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }

        var bare = test.spawn(TTEntities.THAUMATURGE_GOLEM.get(), DEPLOY.west(2));
        bare.setValidSpawn();
        bare.applyBuild(GolemProperties.createDefault());
        float woundedHealth = equipped.getMaxHealth() - 4;
        for (var golem : new EntityThaumaturgeGolem[] {equipped, bare}) {
            golem.tickCount = 0;
            golem.setNoAi(true);
            golem.setHealth(woundedHealth);
        }
        test.startSequence().thenIdle(63).thenExecute(() -> {
            test.assertTrue(equipped.getHealth() == woundedHealth && bare.getHealth() == woundedHealth,
                    "Neither golem should repair before tick 66");
        }).thenIdle(6).thenExecute(() -> {
            test.assertTrue(equipped.getHealth() == woundedHealth + 1 && bare.getHealth() == woundedHealth,
                    "The Fez should repair at tick 66 while the bare golem is still wounded");
        }).thenIdle(34).thenExecute(() -> {
            test.assertTrue(equipped.getHealth() == woundedHealth + 1 && bare.getHealth() == woundedHealth + 1,
                    "The bare golem should repair at tick 100");
        }).thenSucceed();
    }

    @GameTest(template = "golem_room")
    public static void visorCreditsExperienceOnFirstLethalHit(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        level.addNewPlayer(player);
        try {
            var golem = deployDefaultGolem(test, player);
            float armor = golem.getArmorValue();
            var control = test.spawnWithNoFreeWill(EntityType.PIG, DEPLOY.east());
            var target = test.spawnWithNoFreeWill(EntityType.PIG, DEPLOY.east(2));
            control.setHealth(1);
            target.setHealth(1);
            var experience = new AtomicInteger();
            Consumer<LivingExperienceDropEvent> observe = event -> {
                if (event.getEntity() == target || event.getEntity() == control) {
                    experience.set(event.getAttackingPlayer() == player ? event.getDroppedExperience() : -1);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(observe);
            try {
                Objects.requireNonNull(golem.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(8);
                test.assertTrue(golem.doHurtTarget(control) && !control.isAlive() && experience.get() == 0,
                        "An unequipped golem should not award player experience");
                fit(test, player, golem, new ItemStack(TTItems.GOLEM_VISOR.get()));
                test.assertTrue(golem.getArmorValue() == armor + 1, "The Visor should add one armor point");
                Objects.requireNonNull(golem.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(8);
                target.setInvulnerable(true);
                test.assertTrue(!golem.doHurtTarget(target) && target.getKillCredit() == null,
                        "Rejected damage should not give the owner kill credit");
                target.setInvulnerable(false);
                test.assertTrue(golem.doHurtTarget(target) && !target.isAlive(), "The first Visor hit should be lethal");
                test.assertTrue(experience.get() > 0, "The first lethal Visor hit should award owner-credited experience");
                test.assertTrue(target.lastHurtByPlayerTime == 100, "Visor credit should use the native player timeout");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(observe);
            }
            test.succeed();
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    @GameTest(template = "golem_room")
    public static void visorCreditsExperienceFromDart(GameTestHelper test) {
        SealAutomationGameTests.prepareRoom(test);
        var player = ResearchGameTests.survivalPlayer(test);
        var placer = new ItemStack(TTItems.GOLEM_PLACER.get());
        ItemData.set(placer, TTDataComponents.GOLEM_PROPERTIES.get(), GolemProperties.createDefault());
        deploy(test, player, placer);
        test.startSequence().thenWaitUntil(() -> test.assertTrue(
                test.getEntities(TTEntities.THAUMATURGE_GOLEM.get(), DEPLOY, 1.5).stream()
                        .anyMatch(golem -> player.getUUID().equals(golem.getOwnerUUID())),
                "Waiting for the deployed golem to enter entity tracking")).thenExecute(() -> {
            var golem = test.getEntities(TTEntities.THAUMATURGE_GOLEM.get(), DEPLOY, 1.5).stream()
                    .filter(entity -> player.getUUID().equals(entity.getOwnerUUID())).findFirst().orElseThrow();
            verifyVisorDart(test, player, golem);
        });
    }

    private static void verifyVisorDart(GameTestHelper test, FakePlayer player, EntityThaumaturgeGolem golem) {
        var level = test.getLevel();
        var target = test.spawnWithNoFreeWill(EntityType.PIG, DEPLOY.east(3));
        target.setHealth(1);
        var experience = new AtomicInteger();
        Consumer<LivingExperienceDropEvent> observe = event -> {
            if (event.getEntity() == target && event.getAttackingPlayer() == player) {
                experience.set(event.getDroppedExperience());
            }
        };
        level.addNewPlayer(player);
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            fit(test, player, golem, new ItemStack(TTItems.GOLEM_VISOR.get()));
            golem.setNoAi(true);
            EntityGolemDart.loose(TTEntities.GOLEM_DART.get(), golem, target, 8, 0, 1.6F, 0);
            test.runAfterDelay(16, () -> {
                try {
                    test.assertTrue(!target.isAlive() && target.getLastHurtByMob() == golem,
                            "The native golem dart should land one lethal hit");
                    test.assertTrue(experience.get() > 0, "The Visor should credit the owner for the dart kill");
                    test.succeed();
                } finally {
                    MinecraftForge.EVENT_BUS.unregister(observe);
                    level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                }
            });
        } catch (RuntimeException | Error failure) {
            MinecraftForge.EVENT_BUS.unregister(observe);
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            throw failure;
        }
    }

    @GameTest(template = "golem_room")
    public static void topHatPreservesFullHealthAcrossReload(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        level.addNewPlayer(player);
        try {
            var golem = deployDefaultGolem(test, player);
            float baseHealth = golem.getMaxHealth();
            golem.setHealth(baseHealth);
            fit(test, player, golem, new ItemStack(TTItems.GOLEM_TOP_HAT.get()));
            test.assertTrue(golem.getMaxHealth() == baseHealth + 5, "The fitted hat did not add five maximum health");
            golem.heal(5);
            float savedHealth = golem.getHealth();
            test.assertTrue(savedHealth == golem.getMaxHealth(), "The golem did not heal into its bonus health");

            var saved = new CompoundTag();
            test.assertTrue(golem.save(saved), "The equipped golem did not serialize");
            var restored = Objects.requireNonNull(TTEntities.THAUMATURGE_GOLEM.get().create(level));
            restored.load(saved);
            test.assertTrue(restored.getMaxHealth() == savedHealth && restored.getHealth() == savedHealth,
                    "Reload lost some of the golem's saved bonus health: " + restored.getHealth() + " / " + savedHealth);
            test.succeed();
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    @GameTest(template = "golem_room", timeoutTicks = 300)
    public static void pressBuildsAndDeploysOwnedGolemAcrossSaves(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        Vec3 approach = Vec3.atBottomCenterOf(test.absolutePos(PRESS.north()));
        player.moveTo(approach.x, approach.y, approach.z);
        formPress(test, player);

        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(PRESS).is(TTBlocks.GOLEM_BUILDER.get())
                            && test.getBlockState(PRESS.west()).is(TTBlocks.PLACEHOLDER_CAULDRON.get())
                            && test.getBlockState(ANVIL).is(TTBlocks.PLACEHOLDER_ANVIL.get())
                            && test.getBlockState(PRESS.south()).is(TTBlocks.PLACEHOLDER_TABLE.get())
                            && test.getBlockState(PRESS.above()).is(TTBlocks.PLACEHOLDER_IRON_BARS.get()),
                    "Waiting for all five press blocks to transform");
        }).thenExecute(() -> queueGolem(test, player)).thenIdle(12).thenExecute(() -> {
            test.assertTrue(builder(test).cost() == MACHINA_COST && output(test).isEmpty(),
                    "The press advanced without Machina");
            bufferAndReload(test, player);
            stockMachinaJar(test);
        }).thenWaitUntil(() -> {
            test.assertTrue(builder(test).pendingGolem() == null && output(test).is(TTItems.GOLEM_PLACER.get()),
                    "Waiting for the press to deliver the golem");
        }).thenExecute(() -> {
            var jar = (BlockEntityJar) test.getBlockEntity(JAR);
            test.assertTrue(jar.amount() == 4, "The press spent an extra Machina after reloading its buffer");
            var placer = takeOutput(test, player);
            deploy(test, player, placer);
        }).thenWaitUntil(() -> verifyGolemSave(test, player)).thenSucceed();
    }

    private static void formPress(GameTestHelper test, FakePlayer player) {
        test.setBlock(PRESS, Blocks.PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        test.setBlock(PRESS.above(), Blocks.IRON_BARS);
        test.setBlock(PRESS.west(), Blocks.CAULDRON);
        test.setBlock(ANVIL.below(), Blocks.SMOOTH_STONE);
        test.setBlock(ANVIL, Blocks.ANVIL);
        test.setBlock(PRESS.south(), TTBlocks.TABLE_STONE.get());
        var dust = new ItemStack(TTItems.SALIS_MUNDUS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, dust);
        var use = new UseOnContext(player, InteractionHand.MAIN_HAND, topOf(test.absolutePos(PRESS)));
        test.assertTrue(!dust.useOn(use).consumesAction() && dust.getCount() == 1,
                "The locked press blueprint consumed Salis Mundus");
        ResearchManager.complete(player, TTIds.rl("mind_clockwork"));
        ResearchManager.complete(player, TTIds.rl("mat_stud_wood"));
        test.assertTrue(dust.useOn(use).consumesAction() && dust.isEmpty(),
                "The researched golem press blueprint did not consume one Salis Mundus");
    }

    private static void queueGolem(GameTestHelper test, FakePlayer player) {
        openPress(test, player);
        test.setBlock(CHEST, Blocks.CHEST);
        var chest = (ChestBlockEntity) test.getBlockEntity(CHEST);
        chest.setItem(0, new ItemStack(TTBlocks.PLANK_GREATWOOD.get(), 6));
        var inventory = player.getInventory();
        inventory.setItem(1, new ItemStack(TTItems.MIND_CLOCKWORK.get()));
        inventory.setItem(2, new ItemStack(TTItems.MECHANISM_SIMPLE.get(), 3));

        var design = GolemProperties.createDefault().withArms(TTGolemParts.ARMS_FINE.get()).withRank(6);
        var request = new ServerboundGolemPressPayload(test.absolutePos(PRESS), design, true);
        var context = new GameTestPayloadContext(player);
        var builder = builder(test);
        ServerboundGolemPressPayload.handle(request, context);
        test.assertTrue(builder.pendingGolem() == null && chest.getItem(0).getCount() == 6
                        && inventory.getItem(1).getCount() == 1 && inventory.getItem(2).getCount() == 3,
                "The unresearched arms charged construction materials");
        ResearchManager.complete(player, TTIds.rl("mat_stud_brass"));
        var missing = inventory.removeItem(1, 1);
        ServerboundGolemPressPayload.handle(request, context);
        test.assertTrue(builder.pendingGolem() == null && chest.getItem(0).getCount() == 6
                        && inventory.getItem(2).getCount() == 3,
                "An incomplete bill of materials partially charged the inventories");
        inventory.setItem(1, missing);

        ServerboundGolemPressPayload.handle(request, context);
        ServerboundGolemPressPayload.handle(request, context);
        test.assertTrue(builder.cost() == MACHINA_COST && builder.totalCost() == MACHINA_COST
                        && builder.pendingGolem() != null && builder.pendingGolem().rank() == 0
                        && chest.getItem(0).getCount() == 2
                        && inventory.getItem(1).isEmpty() && inventory.getItem(2).isEmpty(),
                "The press did not charge exactly one bill or accepted the requested rank");
    }

    private static void bufferAndReload(GameTestHelper test, FakePlayer player) {
        var level = test.getLevel();
        var pos = test.absolutePos(PRESS);
        var original = builder(test);
        var conduit = original.getCapability(EssentiaCapabilities.CONDUIT, Direction.NORTH);
        var input = conduit.orElseThrow(IllegalStateException::new);
        var machina = Objects.requireNonNull(Aspects.resolve(level, TTAspects.MACHINA));
        var ordo = Objects.requireNonNull(Aspects.resolve(level, TTAspects.ORDO));
        var chunk = level.getChunkAt(pos);
        chunk.setUnsaved(false);
        test.assertTrue(input.insert(ordo, 1, Direction.NORTH, false) == 0
                        && input.insert(machina, 1, Direction.NORTH, true) == 1
                        && input.roomFor(machina, Direction.NORTH) == 1 && !chunk.isUnsaved(),
                "Rejected or simulated input altered the press");
        test.assertTrue(input.insert(machina, 1, Direction.NORTH, false) == 1
                        && input.roomFor(machina, Direction.NORTH) == 0 && chunk.isUnsaved(),
                "Buffering Machina did not store one unit and mark the chunk dirty");

        player.closeContainer();
        var saved = original.saveWithFullMetadata();
        level.removeBlockEntity(pos);
        var replacement = new BlockEntityGolemBuilder(pos, test.getBlockState(PRESS));
        level.setBlockEntity(replacement);
        replacement.load(saved);
        var newInput = replacement.getCapability(EssentiaCapabilities.CONDUIT, Direction.NORTH)
                .orElseThrow(IllegalStateException::new);
        test.assertTrue(builder(test) == replacement && !conduit.isPresent(),
                "Replacing the press did not invalidate the old Forge capability");
        test.assertTrue(replacement.pendingGolem() != null && replacement.cost() == MACHINA_COST,
                "Native press reload lost its assembly job");
        test.assertTrue(newInput.roomFor(machina, Direction.NORTH) == 0,
                "Native press reload lost its buffered Machina");
    }

    private static void stockMachinaJar(GameTestHelper test) {
        test.setBlock(JAR, TTBlocks.JAR_NORMAL.get());
        var jar = (BlockEntityJar) test.getBlockEntity(JAR);
        jar.setAspectFilter(TTAspects.MACHINA);
        var machina = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.MACHINA));
        var top = jar.getCapability(EssentiaCapabilities.CONDUIT, Direction.UP).orElseThrow(IllegalStateException::new);
        test.assertTrue(top.insert(machina, 17, Direction.UP, false) == 17, "The jar did not accept the press supply");
    }

    private static ItemStack takeOutput(GameTestHelper test, FakePlayer player) {
        var menu = openPress(test, player);
        menu.clicked(OUTPUT, 0, ClickType.PICKUP, player);
        var placer = menu.getCarried();
        var design = ItemData.get(placer, TTDataComponents.GOLEM_PROPERTIES.get());
        test.assertTrue(output(test).isEmpty() && placer.getCount() == 1 && hasExpectedParts(design),
                "The output did not yield one unranked wooden golem with fine arms");
        menu.setCarried(ItemStack.EMPTY);
        player.closeContainer();
        return placer;
    }

    private static void deploy(GameTestHelper test, FakePlayer player, ItemStack placer) {
        test.setBlock(DEPLOY.below(), Blocks.SMOOTH_STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND, placer);
        var use = new UseOnContext(player, InteractionHand.MAIN_HAND, topOf(test.absolutePos(DEPLOY.below())));
        test.assertTrue(placer.useOn(use).consumesAction() && placer.isEmpty(),
                "Survival deployment did not consume one golem item");
    }

    private static EntityThaumaturgeGolem deployDefaultGolem(GameTestHelper test, FakePlayer player) {
        var placer = new ItemStack(TTItems.GOLEM_PLACER.get());
        ItemData.set(placer, TTDataComponents.GOLEM_PROPERTIES.get(), GolemProperties.createDefault());
        deploy(test, player, placer);
        var golems = test.getEntities(TTEntities.THAUMATURGE_GOLEM.get(), DEPLOY, 1.5);
        test.assertTrue(golems.size() == 1, "Expected one deployed golem");
        return golems.get(0);
    }

    private static void fit(GameTestHelper test, FakePlayer player, EntityThaumaturgeGolem golem, ItemStack accessory) {
        String itemName = accessory.getHoverName().getString();
        player.setItemInHand(InteractionHand.MAIN_HAND, accessory);
        test.assertTrue(golem.interact(player, InteractionHand.MAIN_HAND).consumesAction() && accessory.isEmpty(),
                "The owner could not fit " + itemName);
    }

    private static void verifyGolemSave(GameTestHelper test, FakePlayer player) {
        var golems = test.getEntities(TTEntities.THAUMATURGE_GOLEM.get(), DEPLOY, 1.5);
        test.assertTrue(golems.size() == 1, "Expected exactly one deployed golem");
        var golem = golems.get(0);
        test.assertTrue(player.getUUID().equals(golem.getOwnerUUID()) && hasExpectedParts(golem.properties()),
                "Deployment lost the owner or golem parts");
        var saved = new CompoundTag();
        test.assertTrue(golem.save(saved), "The registered golem did not serialize");
        EntityThaumaturgeGolem restored = Objects.requireNonNull(TTEntities.THAUMATURGE_GOLEM.get().create(test.getLevel()));
        restored.load(saved);
        test.assertTrue(golem.getUUID().equals(restored.getUUID()) && player.getUUID().equals(restored.getOwnerUUID())
                        && hasExpectedParts(restored.properties()),
                "Native golem NBT lost identity, owner, or build");
    }

    private static boolean hasExpectedParts(GolemProperties design) {
        return design != null && design.rank() == 0 && design.material() == TTGolemParts.WOOD.get()
                && design.head() == TTGolemParts.HEAD_BASIC.get() && design.arms() == TTGolemParts.ARMS_FINE.get()
                && design.legs() == TTGolemParts.LEGS_WALKER.get() && design.addon() == TTGolemParts.ADDON_NONE.get();
    }

    private static MenuGolemBuilder openPress(GameTestHelper test, FakePlayer player) {
        var hit = topOf(test.absolutePos(PRESS));
        test.getBlockState(PRESS).use(test.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        test.assertTrue(player.containerMenu instanceof MenuGolemBuilder, "The real press did not open its Forge menu");
        return (MenuGolemBuilder) player.containerMenu;
    }

    private static BlockEntityGolemBuilder builder(GameTestHelper test) {
        return (BlockEntityGolemBuilder) test.getBlockEntity(PRESS);
    }

    private static ItemStack output(GameTestHelper test) {
        return builder(test).outputHandler().getStackInSlot(OUTPUT);
    }

    private static BlockHitResult topOf(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
    }
}
