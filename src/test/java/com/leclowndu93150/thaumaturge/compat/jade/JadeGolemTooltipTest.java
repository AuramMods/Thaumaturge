package com.leclowndu93150.thaumaturge.compat.jade;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

class JadeGolemTooltipTest {
    @Test
    void experienceRequiresServerDataForTheCurrentRank() {
        var golem = mock(EntityThaumaturgeGolem.class);
        when(golem.properties()).thenReturn(
                GolemProperties.createDefault().withHead(TTGolemParts.HEAD_SMART.get()).withRank(2));
        var accessor = mock(EntityAccessor.class);
        when(accessor.getEntity()).thenReturn(golem);
        when(accessor.showDetails()).thenReturn(true);
        var data = new CompoundTag();
        when(accessor.getServerData()).thenReturn(data);
        var config = mock(IPluginConfig.class);
        when(config.get(JadeConfig.CATEGORY)).thenReturn(true);
        when(config.<JadeConfig.DisplayMode>getEnum(JadeConfig.GOLEMS)).thenReturn(JadeConfig.DisplayMode.ON);

        assertRankOnly(accessor, config);
        data.putInt(GolemDataProvider.RANK_XP_REQUIRED, 9000);
        assertRankOnly(accessor, config);
        data.putInt(GolemDataProvider.RANK_XP, 311);
        data.putInt(GolemDataProvider.RANK_XP_REQUIRED, 4000);
        assertRankOnly(accessor, config);

        data.putInt(GolemDataProvider.RANK_XP_REQUIRED, 9000);
        data.putInt(GolemDataProvider.RANK_XP, 0);
        var tooltip = mock(ITooltip.class);
        GolemComponentProvider.INSTANCE.appendTooltip(tooltip, accessor, config);
        verify(tooltip).add(Component.translatable("jade.thaumaturge.golem.rank", 2));
        verify(tooltip).add(Component.translatable("jade.thaumaturge.golem.xp", 0, 9000));
        verifyNoMoreInteractions(tooltip);
    }

    private static void assertRankOnly(EntityAccessor accessor, IPluginConfig config) {
        var tooltip = mock(ITooltip.class);
        GolemComponentProvider.INSTANCE.appendTooltip(tooltip, accessor, config);
        verify(tooltip).add(Component.translatable("jade.thaumaturge.golem.rank", 2));
        verifyNoMoreInteractions(tooltip);
    }
}
