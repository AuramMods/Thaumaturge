package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.client.extensions.ArmorModelClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ModeledArmorItem extends ArmorItem {
    public ModeledArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ArmorModelClientExtensions.INSTANCE);
    }
}
