package com.leclowndu93150.thaumaturge.content.entity.loot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

class MobSpawnEquipmentRuntimeTest {
    @Test
    void sourceEquipmentTableLoadsAndAssignsOnlyTheFirstItemForEachSlot(@TempDir Path pack) throws Exception {
        var path = pack.resolve("data/thaumaturge/loot_tables/equipment/cultist_knight.json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, """
                {"type":"minecraft:equipment","pools":[
                  {"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:arrow",
                   "functions":[{"function":"minecraft:set_count","count":12}]}]},
                  {"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:iron_sword"}]},
                  {"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:chainmail_helmet"}]},
                  {"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:diamond_helmet"}]},
                  {"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:iron_boots"}]}
                ]}
                """);

        var lootData = new LootDataManager();
        // Initialize Forge's empty modifier manager. Its reload wrapper only participates
        // in the reload barrier after full mod loading, which this launcher skips.
        var reload = new AddReloadListenerEvent(mock(ReloadableServerResources.class), RegistryAccess.EMPTY);
        new ForgeInternalHandler().onResourceReload(reload);
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources("mob-equipment-fixture", pack, false)))) {
            SimpleReloadInstance.of(resources, List.of(lootData), Runnable::run, Runnable::run,
                    CompletableFuture.completedFuture(Unit.INSTANCE)).done().get(10, TimeUnit.SECONDS);
        }

        var params = MobSpawnEquipment.PARAMETERS;
        assertEquals(ResourceLocation.withDefaultNamespace("equipment"), LootContextParamSets.getKey(params));
        assertEquals(Set.of(LootContextParams.THIS_ENTITY, LootContextParams.ORIGIN), params.getRequired());
        assertNotSame(LootTable.EMPTY, lootData.getLootTable(TTLootTables.EQUIPMENT_CULTIST_KNIGHT.location()));
        var server = mock(MinecraftServer.class);
        var level = mock(ServerLevel.class);
        var mob = mock(Mob.class);
        when(server.getLootData()).thenReturn(lootData);
        when(level.getServer()).thenReturn(server);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        when(level.getRandom()).thenReturn(RandomSource.create(642));
        when(mob.level()).thenReturn(level);
        when(mob.position()).thenReturn(new Vec3(10, 64, 12));

        MobSpawnEquipment.equip(mob, TTLootTables.EQUIPMENT_CULTIST_KNIGHT,
                Map.of(EquipmentSlot.HEAD, 0.15F, EquipmentSlot.CHEST, 0.05F));
        var mainHand = ArgumentCaptor.forClass(ItemStack.class);
        verify(mob).setItemSlot(eq(EquipmentSlot.MAINHAND), mainHand.capture());
        assertTrue(mainHand.getValue().is(Items.ARROW));
        assertEquals(12, mainHand.getValue().getCount());
        verify(mob).setItemSlot(eq(EquipmentSlot.HEAD), argThat(stack -> stack.is(Items.CHAINMAIL_HELMET)));
        verify(mob).setItemSlot(eq(EquipmentSlot.FEET), argThat(stack -> stack.is(Items.IRON_BOOTS)));
        verify(mob, times(3)).setItemSlot(any(), any());
        verify(mob).setDropChance(EquipmentSlot.HEAD, 0.15F);
        verify(mob, times(1)).setDropChance(any(), anyFloat());

        clearInvocations(mob);
        MobSpawnEquipment.equip(mob, TTLootTables.EQUIPMENT_CULTIST_LEADER, Map.of());
        verify(mob, never()).setItemSlot(any(), any());
    }

    @Test
    void spawnEnchantUsesSourceCostRangeAndKeepsStrongerExistingEnchantments() {
        var difficulty = new DifficultyInstance(Difficulty.HARD, 4_000_000, 4_000_000, 1.0F);
        assertEquals(1.0F, difficulty.getSpecialMultiplier());
        for (int seed = 0; seed < 32; seed++) {
            var stack = new ItemStack(Items.IRON_SWORD);
            stack.enchant(Enchantments.SHARPNESS, 5);
            var reference = RandomSource.create(seed);
            int cost = 5 + reference.nextInt(18);
            var expected = EnchantmentHelper.getEnchantments(stack);
            for (var enchantment : EnchantmentHelper.selectEnchantment(reference, stack, cost, false)) {
                expected.merge(enchantment.enchantment, enchantment.level, Math::max);
            }
            MobSpawnEquipment.enchant(stack, RandomSource.create(seed), difficulty);
            assertEquals(expected, EnchantmentHelper.getEnchantments(stack), "seed " + seed);
        }
    }
}
