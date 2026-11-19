package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class EssentiaGameTests {
    private static final String ROOM = "essentia_room";
    private static final BlockPos JAR = new BlockPos(2, 1, 2);

    private EssentiaGameTests() {}

    @GameTest(template = ROOM)
    public static void jarSidedCapabilityAndNativeSave(GameTestHelper test) {
        test.setBlock(JAR, TTBlocks.JAR_NORMAL.get());
        var jar = blockEntity(test, JAR, BlockEntityJar.class);
        var order = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.ORDO));
        var fire = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.IGNIS));
        jar.setAspectFilter(TTAspects.ORDO);
        var topCapability = jar.getCapability(EssentiaCapabilities.CONDUIT, Direction.UP);
        var top = topCapability.orElseThrow(() -> new IllegalStateException("Missing top conduit"));

        test.assertFalse(jar.getCapability(EssentiaCapabilities.CONDUIT, Direction.NORTH).isPresent(),
                "A side face exposed the jar conduit");
        test.assertTrue(top.insert(fire, 1, Direction.UP, false) == 0, "Label accepted the wrong aspect");
        test.assertTrue(top.insert(order, 24, Direction.UP, true) == 24 && jar.amount() == 0,
                "A simulated insert committed");
        test.assertTrue(top.insert(order, 24, Direction.UP, false) == 24, "Insertion failed");
        var saved = jar.saveWithFullMetadata();

        test.setBlock(JAR, Blocks.AIR);
        test.assertFalse(topCapability.isPresent(), "Removing the jar left its capability valid");
        test.setBlock(JAR, TTBlocks.JAR_NORMAL.get());
        var replacement = blockEntity(test, JAR, BlockEntityJar.class);
        replacement.load(saved);
        test.assertTrue(replacement != jar, "Expected a fresh block entity");
        test.assertTrue(TTAspects.ORDO.equals(replacement.aspectFilterKey()), "Lost saved jar label");
        test.assertTrue(replacement.amount() == 24 && replacement.heldAspect(Direction.UP) == order,
                "Lost saved essentia or its registry holder");
        test.succeed();
    }

    @GameTest(template = ROOM)
    public static void filledJarPlacementAndLootKeepEssentia(GameTestHelper test) {
        test.setBlock(JAR.below(), Blocks.SMOOTH_STONE);
        var order = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.ORDO));
        ItemStack filled = new ItemStack(TTItems.JAR_NORMAL.get());
        ItemData.set(filled, TTDataComponents.ASPECT_FILTER.get(), TTAspects.ORDO);
        ItemData.set(filled, TTDataComponents.ESSENTIA_CONTENTS.get(), new EssentiaList(AspectList.EMPTY.add(order, 19)));
        ItemStack copy = filled.copy();
        var player = test.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, copy);
        BlockPos support = test.absolutePos(JAR.below());
        var hit = new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false);

        var result = copy.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        test.assertTrue(result.consumesAction(), "Jar item placement was rejected");
        test.assertTrue(copy.isEmpty(), "Survival placement did not consume the placed stack");
        test.assertTrue(filled.getCount() == 1, "Placing a copied stack changed the original");
        var placed = blockEntity(test, JAR, BlockEntityJar.class);
        test.assertTrue(TTAspects.ORDO.equals(placed.aspectFilterKey()) && placed.amount() == 19,
                "Jar placement lost its label or contents");

        test.assertTrue(test.getLevel().destroyBlock(test.absolutePos(JAR), true), "Jar break failed");
        test.succeedWhen(() -> {
            var items = test.getEntities(EntityType.ITEM, JAR, 1.5);
            var drops = items.stream().map(entity -> entity.getItem())
                    .filter(stack -> stack.is(TTItems.JAR_NORMAL.get())).toList();
            test.assertTrue(drops.size() == 1 && drops.get(0).getCount() == 1,
                    "Expected exactly one jar drop among " + items.stream().map(entity -> entity.getItem()).toList());
            ItemStack dropped = drops.get(0);
            test.assertTrue(TTAspects.ORDO.equals(ItemData.get(dropped, TTDataComponents.ASPECT_FILTER.get())),
                    "Broken jar lost its label");
            var contents = ItemData.get(dropped, TTDataComponents.ESSENTIA_CONTENTS.get());
            test.assertTrue(contents != null && contents.totalAmount() == 19
                            && contents.contents().entries().get(0).aspect() == order,
                    "Broken jar lost the registered aspect or its amount");
        });
    }

    @GameTest(template = ROOM, timeoutTicks = 160)
    public static void labelledJarPullsThroughTube(GameTestHelper test) {
        var tubePos = JAR.above();
        var alembicPos = tubePos.east();
        test.setBlock(JAR, TTBlocks.JAR_NORMAL.get());
        test.setBlock(tubePos, TTBlocks.TUBE.get());
        test.setBlock(alembicPos, TTBlocks.ALEMBIC.get());
        var jar = blockEntity(test, JAR, BlockEntityJar.class);
        var tube = blockEntity(test, tubePos, BlockEntityTube.class);
        var alembic = blockEntity(test, alembicPos, BlockEntityAlembic.class);
        jar.setAspectFilter(TTAspects.ORDO);
        alembic.aimSpout(Direction.EAST);
        alembic.setStorageContents(TTAspects.ORDO, 6);
        BlockEssentiaTransport.refreshConnectionsAround(test.getLevel(), test.absolutePos(tubePos));

        test.succeedWhen(() -> {
            test.assertTrue(jar.amount() == 6, "Waiting for all six points to reach the jar");
            test.assertTrue(alembic.amount() == 0 && tube.essentiaAmountRaw() == 0,
                    "Transfer duplicated essentia in the tray or tube");
            test.assertTrue(TTAspects.ORDO.equals(jar.aspectKey()), "Jar received the wrong aspect");
        });
    }

    private static <T extends BlockEntity> T blockEntity(GameTestHelper test, BlockPos pos, Class<T> type) {
        var entity = test.getBlockEntity(pos);
        test.assertTrue(type.isInstance(entity), "Missing " + type.getSimpleName() + " at " + pos);
        return type.cast(entity);
    }
}
