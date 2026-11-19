package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.Test;

class EntityMindSpiderRuntimeTest {
    @Test
    void normalSpidersUseNativeEquipmentExperienceAndHarmlessSpidersStayAtZeroAcrossReload() throws Exception {
        var type = registerType();
        var attributes = attributes();
        new EntityAttributeCreationEvent(attributes).put(type, EntityMindSpider.createAttributes().build());
        try {
            var level = mock(Level.class);
            var spider = new EntityMindSpider(type, level);
            assertEquals(1, spider.getExperienceReward());
            spider.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            int experience = spider.getExperienceReward();
            assertTrue(experience >= 2 && experience <= 4);

            spider.setHarmless(true);
            spider.setViewer("PortViewer");
            assertEquals(0, spider.getExperienceReward());
            var saved = new CompoundTag();
            spider.addAdditionalSaveData(saved);

            var restored = new EntityMindSpider(type, level);
            restored.readAdditionalSaveData(saved);
            assertTrue(restored.isHarmless());
            assertEquals("PortViewer", restored.getViewer());
            assertEquals(0, restored.getExperienceReward());
            restored.setHarmless(false);
            experience = restored.getExperienceReward();
            assertTrue(experience >= 2 && experience <= 4);
        } finally {
            attributes.remove(type);
        }
    }

    private static EntityType<EntityMindSpider> registerType() {
        var id = new ResourceLocation("port_mind_spider", "spider");
        var forge = (ForgeRegistry<?>) ForgeRegistries.ENTITY_TYPES;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.ENTITY_TYPE;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            var type = EntityType.Builder.of(EntityMindSpider::new, MobCategory.MONSTER)
                    .sized(0.3F, 0.3F).build(id.toString());
            ForgeRegistries.ENTITY_TYPES.register(id, type);
            return type;
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> attributes() throws Exception {
        var field = ForgeHooks.class.getDeclaredField("FORGE_ATTRIBUTES");
        field.setAccessible(true);
        return (Map<EntityType<? extends LivingEntity>, AttributeSupplier>) field.get(null);
    }
}
