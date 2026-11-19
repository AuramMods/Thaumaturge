package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EntityGeometryRuntimeTest {
    private static RegistryObject<EntityType<ProbeProjectile>> projectileType;
    private static RegistryObject<EntityType<Slime>> slimeType;
    private static RegistryObject<EntityType<Zombie>> zombieType;

    @BeforeAll
    static void registerFixtureTypes() throws Exception {
        var types = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "port_geometry");
        projectileType = types.register("projectile", () -> EntityGeometry.attach(
                EntityType.Builder.of(ProbeProjectile::new, MobCategory.MISC)
                        .sized(0.3F, 0.4F).noSave().build("port_geometry:projectile"), 0.15F, 1.0F));
        slimeType = types.register("slime", () -> EntityGeometry.attach(
                EntityType.Builder.of(Slime::new, MobCategory.MONSTER)
                        .sized(0.52F, 0.52F).noSave().build("port_geometry:slime"), 0.325F, 4.0F));
        zombieType = types.register("zombie", () -> EntityGeometry.attach(
                EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.MONSTER)
                        .sized(0.6F, 1.95F).noSave().build("port_geometry:zombie"), 1.7F, 1.0F));
        var bus = BusBuilder.builder().build();
        types.register(bus);
        var registry = (ForgeRegistry<?>) ForgeRegistries.ENTITY_TYPES;
        var view = (MappedRegistry<?>) BuiltInRegistries.ENTITY_TYPE;
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        registry.unfreeze();
        view.unfreeze();
        try {
            bus.post(ctor.newInstance(ForgeRegistries.Keys.ENTITY_TYPES, registry, view));
        } finally {
            registry.freeze();
            view.freeze();
        }
    }

    @Test
    @SuppressWarnings("removal")
    void nativeConstructorRefreshAndPoseQueriesUseTheAuthoredHeight() {
        var type = projectileType.get();
        var entity = new ProbeProjectile(type, mock(Level.class));
        assertEquals(0.15F, entity.getEyeHeight());
        assertEquals(0.15F, entity.getEyeHeight(Pose.STANDING));
        assertEquals(0.075F, entity.getEyeHeightAccess(Pose.STANDING, type.getDimensions().scale(0.5F)));

        Consumer<EntityEvent.Size> resize = event -> {
            if (event.getEntity() == entity) {
                assertEquals(0.15F, event.getNewEyeHeight());
                event.setNewSize(event.getNewSize().scale(0.5F), true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(resize);
        try {
            entity.refreshDimensions();
            assertEquals(0.075F, entity.getEyeHeight());
            assertEquals(0.2F, entity.getBbHeight());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(resize);
        }
    }

    @Test
    void slimeUsesAuthoredBaseSizeAndSeparateSpawnClearance() throws Exception {
        var type = slimeType.get();
        var level = mock(Level.class);
        var attributes = attributes();
        new EntityAttributeCreationEvent(attributes).put(type, DefaultAttributes.getSupplier(EntityType.SLIME));
        try {
            var slime = new Slime(type, level);
            slime.setSize(3, true);
            assertEquals(0.52F * 3, slime.getBbWidth());
            assertEquals(0.52F * 3, slime.getBbHeight());
            assertEquals(0.325F * 3, slime.getEyeHeight(), 1.0e-6F);
            var spawn = type.getAABB(3.0, 22.0, -7.0);
            assertEquals(0.52F * 4, spawn.getXsize(), 1.0e-6);
            assertEquals(0.52F * 4, spawn.getYsize(), 1.0e-6);
            assertEquals(22.0, spawn.minY);
            assertEquals(3.0, spawn.getCenter().x);
            assertEquals(-7.0, spawn.getCenter().z);

            var vanilla = new Slime(EntityType.SLIME, level);
            vanilla.setSize(3, true);
            assertEquals(EntityType.SLIME.getWidth() * (0.255F * 3), vanilla.getBbWidth());
            assertEquals(EntityType.SLIME.getWidth(), EntityType.SLIME.getAABB(0, 0, 0).getXsize(), 1.0e-6);
        } finally {
            attributes.remove(type);
        }
    }

    @Test
    void babyAndAuthoredAdultGeometryScaleOnceWhileSleepingSizeStaysFixed() throws Exception {
        var type = zombieType.get();
        var attributes = attributes();
        var scale = TTAttributes.SCALE.get();
        var supplier = new AttributeSupplier.Builder(DefaultAttributes.getSupplier(EntityType.ZOMBIE)).add(scale);
        new EntityAttributeCreationEvent(attributes).put(type, supplier.build());
        try {
            var zombie = new Zombie(type, mock(Level.class));
            assertEquals(1.7F, zombie.getEyeHeight());
            assertEquals(0.2F, zombie.getEyeHeight(Pose.SLEEPING));
            zombie.setBaby(true);
            assertEquals(0.93F, zombie.getEyeHeight());
            assertEquals(0.93F, zombie.getEyeHeight(Pose.STANDING));
            assertEquals(0.2F, zombie.getEyeHeight(Pose.SLEEPING));
            float babyWidth = zombie.getBbWidth();
            zombie.getAttribute(scale).setBaseValue(2.5);
            zombie.refreshDimensions();
            assertEquals(babyWidth * 2.5F, zombie.getBbWidth());
            assertEquals(0.93F * 2.5F, zombie.getEyeHeight());
            assertEquals(0.93F * 2.5F, zombie.getEyeHeight(Pose.STANDING));
            zombie.setPose(Pose.SLEEPING);
            assertEquals(0.2F, zombie.getEyeHeight());
            zombie.setPose(Pose.STANDING);
            zombie.setBaby(false);
            assertEquals(type.getWidth() * 2.5F, zombie.getBbWidth());
            assertEquals(1.7F * 2.5F, zombie.getEyeHeight());
        } finally {
            attributes.remove(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes() throws Exception {
        var field = ForgeHooks.class.getDeclaredField("FORGE_ATTRIBUTES");
        field.setAccessible(true);
        return (Map<EntityType<? extends LivingEntity>, AttributeSupplier>) field.get(null);
    }

    private static final class ProbeProjectile extends ThrowableProjectile {
        ProbeProjectile(EntityType<? extends ProbeProjectile> type, Level level) {
            super(type, level);
        }

        @Override protected void defineSynchedData() {}
    }
}
