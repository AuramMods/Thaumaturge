package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aurammods.thaumaturge.testing.runtime.ForgePortTestRunner;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitAttributeModifiers;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintBroodTrait;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.mojang.serialization.Lifecycle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

class EntityScaleRuntimeTest {
    @Test
    void scaleIsRegisteredWithTheBaselineLimitsAndAvailableToLivingTypes() {
        var scale = TTAttributes.SCALE.get();
        assertEquals(ResourceLocation.withDefaultNamespace("generic.scale"), ForgeRegistries.ATTRIBUTES.getKey(scale));
        assertTrue(scale.isClientSyncable());
        assertEquals(1.0, scale.getDefaultValue());
        assertEquals(0.0625, scale.sanitizeValue(0.0));
        assertEquals(16.0, scale.sanitizeValue(20.0));

        var builders = new LinkedHashMap<EntityType<? extends LivingEntity>, AttributeSupplier.Builder>();
        ForgePortTestRunner.postModEvent(new EntityAttributeModificationEvent(builders));
        for (var type : new EntityAttributeModificationEvent(builders).getTypes()) {
            assertTrue(builders.get(type).build().hasAttribute(scale), type::toString);
        }
    }

    @Test
    @SuppressWarnings("removal")
    void aNativeTickCoalescesGrowthAndRestorationAndPublishesScaledForgeDimensions() throws Exception {
        try (var attributes = installAttributes(EntityType.ZOMBIE)) {
            var mob = new ProbeLiving();
            float width = mob.getBbWidth();
            float height = mob.getBbHeight();
            float eyes = mob.getEyeHeight();
            var resizeCount = new AtomicInteger();
            Consumer<EntityEvent.Size> onResize = event -> {
                if (event.getEntity() == mob) {
                    resizeCount.incrementAndGet();
                    assertEquals(width * EntityScale.factor(mob), event.getNewSize().width);
                    assertEquals(eyes * EntityScale.factor(mob), event.getNewEyeHeight());
                }
            };
            MinecraftForge.EVENT_BUS.addListener(onResize);
            try {
                var scale = mob.getAttribute(TTAttributes.SCALE.get());
                scale.setBaseValue(2.0);
                scale.setBaseValue(3.0);
                mob.tick();
                assertEquals(1, resizeCount.get());
                assertEquals(width * 3.0F, mob.getBbWidth());
                assertEquals(height * 3.0F, mob.getBbHeight());
                assertEquals(eyes * 3.0F, mob.getEyeHeight());
                assertEquals(mob.getBbWidth(), mob.getBoundingBox().getXsize(), 1.0e-6);
                assertEquals(mob.getBbWidth(), mob.poseBounds().getXsize(), 1.0e-6);
                assertEquals(mob.getBbHeight(), mob.getLocalBoundsForPose(Pose.STANDING).getYsize(), 1.0e-6);
                assertEquals(mob.getBbHeight(), mob.getDimensionsForge(Pose.STANDING).height);
                assertEquals(mob.getEyeHeight(), mob.getEyeHeight(Pose.STANDING));
                assertEquals(0.2F, mob.getEyeHeight(Pose.SLEEPING));
                mob.tick();
                assertEquals(1, resizeCount.get());
                mob.fixupScaledDimensions();
                assertEquals(height * 3.0F, mob.getBbHeight());
                assertEquals(eyes * 3.0F, mob.getEyeHeight());

                scale.setBaseValue(1.0);
                mob.tick();
                assertEquals(2, resizeCount.get());
                assertEquals(width, mob.getBbWidth());
                assertEquals(height, mob.getBbHeight());
                assertEquals(eyes, mob.getEyeHeight());
            } finally {
                MinecraftForge.EVENT_BUS.unregister(onResize);
            }
        }
    }

    @Test
    void nativeSpiderEyesAndHitboxFollowTheBroodTraitThroughSaveAndRemoval() throws Exception {
        try (var attributes = installAttributes(EntityType.SPIDER)) {
            var spider = new Spider(EntityType.SPIDER, mock(Level.class));
            var scale = spider.getAttribute(TTAttributes.SCALE.get());
            float width = spider.getBbWidth();
            float eyes = spider.getEyeHeight();
            var traits = new MappedRegistry<MobTrait>(MobTrait.REGISTRY_KEY, Lifecycle.stable());
            var key = ResourceKey.create(MobTrait.REGISTRY_KEY, TTIds.rl("taint_brood"));
            traits.register(key, new TaintBroodTrait(), Lifecycle.stable());
            var brood = traits.freeze().getHolderOrThrow(key);
            scale.setBaseValue(0.75);
            MobTraitAttributeModifiers.refresh(spider, List.of(brood));
            spider.refreshDimensions();
            assertEquals(width * 0.4F, spider.getBbWidth());
            assertEquals(eyes * 0.4F, spider.getEyeHeight());
            var save = spider.saveWithoutId(new CompoundTag());
            var serialized = save.getList("Attributes", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .filter(attribute -> attribute.getString("Name").equals("minecraft:generic.scale"))
                    .findFirst().orElseThrow();
            assertEquals(0.75, serialized.getDouble("Base"));
            var loaded = new Spider(EntityType.SPIDER, mock(Level.class));
            loaded.load(save);
            loaded.refreshDimensions();
            assertEquals(width * 0.4F, loaded.getBbWidth());
            assertEquals(eyes * 0.4F, loaded.getEyeHeight());

            MobTraitAttributeModifiers.refresh(loaded, List.of());
            loaded.refreshDimensions();
            assertEquals(0.75, loaded.getAttributeValue(TTAttributes.SCALE.get()));
            assertEquals(width * 0.75F, loaded.getBbWidth());
            assertEquals(eyes * 0.75F, loaded.getEyeHeight());
        }
    }

    @Test
    void portalEntryAndExitAccountForTheScaledEntityClearance() throws Exception {
        try (var attributes = installAttributes(EntityType.ZOMBIE)) {
            var mob = new ProbeLiving();
            mob.getAttribute(TTAttributes.SCALE.get()).setBaseValue(3.0);
            mob.tick();
            var rectangle = new BlockUtil.FoundRectangle(BlockPos.ZERO, 4, 8);
            mob.setPos(mob.getBbWidth() / 2.0, (8.0 - mob.getBbHeight()) / 4.0, 0.5);
            Vec3 relative = mob.relativePortalPosition(rectangle);
            assertEquals(0.0, relative.x, 1.0e-6);
            assertEquals(0.25, relative.y, 1.0e-6);
            assertEquals(0.0, relative.z, 1.0e-6);
            var destination = mock(ServerLevel.class);
            when(destination.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.NETHER_PORTAL.defaultBlockState());
            var portal = PortalShape.createPortalInfo(destination, rectangle, Direction.Axis.X, relative, mob,
                    Vec3.ZERO, 0.0F, 0.0F);
            assertEquals(mob.getX(), portal.pos.x, 1.0e-6);
            assertEquals(mob.getY(), portal.pos.y, 1.0e-6);
            assertEquals(mob.getZ(), portal.pos.z, 1.0e-6);
        }
    }

    private static AutoCloseable installAttributes(EntityType<? extends LivingEntity> type) throws Exception {
        var builders = new LinkedHashMap<EntityType<? extends LivingEntity>, AttributeSupplier.Builder>();
        ForgePortTestRunner.postModEvent(new EntityAttributeModificationEvent(builders));
        var supplier = new AttributeSupplier.Builder(DefaultAttributes.getSupplier(type));
        supplier.combine(builders.get(type));
        var field = ForgeHooks.class.getDeclaredField("FORGE_ATTRIBUTES");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var definitions = (Map<EntityType<? extends LivingEntity>, AttributeSupplier>) field.get(null);
        var original = definitions.put(type, supplier.build());
        return () -> {
            if (original == null) definitions.remove(type);
            else definitions.put(type, original);
        };
    }

    private static final class ProbeLiving extends LivingEntity {
        ProbeLiving() {
            super(EntityType.ZOMBIE, mock(Level.class));
            when(level().getProfiler()).thenReturn(InactiveProfiler.INSTANCE);
        }

        AABB poseBounds() { return getBoundingBoxForPose(Pose.STANDING); }
        void fixupScaledDimensions() { fixupDimensions(); }

        Vec3 relativePortalPosition(BlockUtil.FoundRectangle rectangle) {
            return getRelativePortalPosition(Direction.Axis.X, rectangle);
        }

        @Override public void baseTick() {}
        @Override public void aiStep() {}
        @Override public Iterable<ItemStack> getArmorSlots() { return List.of(); }
        @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
        @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}
        @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
    }
}
