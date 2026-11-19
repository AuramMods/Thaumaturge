package com.leclowndu93150.thaumaturge.content.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.infusion.InfusionCraftedEvent;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.cast.TouchBudget;
import com.leclowndu93150.thaumaturge.api.spell.event.ScriptedSpellEvent;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellCastEvent;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellEffectEvent;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellNodeEvent;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.content.spell.script.ScriptedNode;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.junit.jupiter.api.Test;

class SpellEventsRuntimeTest {
    @Test
    void effectKeepsTheBudgetCancellationPowerAndChildTargets() {
        CastContext ctx = context();
        TouchBudget budget = mock(TouchBudget.class);
        when(ctx.budget()).thenReturn(budget);
        SpellTarget noBudget = target(0);
        SpellTarget canceled = target(1);
        SpellTarget boosted = target(2);
        SpellTarget regular = target(3);
        List<SpellTarget> incoming = List.of(noBudget, canceled, boosted, regular);
        when(budget.claim(canceled.blockPos())).thenReturn(true);
        when(budget.claim(boosted.blockPos())).thenReturn(true);
        when(budget.claim(regular.blockPos())).thenReturn(true);
        List<SpellTarget> dispatched = new ArrayList<>();
        Consumer<SpellEffectEvent> listener = event -> {
            if (event.context() != ctx) {
                return;
            }
            dispatched.add(event.target());
            assertEquals(3.0F, event.power());
            if (event.target() == canceled) {
                event.setCanceled(true);
            }
            if (event.target() == boosted) {
                event.setPower(7.5F);
            }
        };

        RecordingEffect effect = new RecordingEffect();
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            effect.execute(ctx, incoming);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(List.of(canceled, boosted, regular), dispatched);
        assertEquals(List.of(new Application(boosted, 7.5F, 0), new Application(regular, 3.0F, 1)), effect.applied);
        verify(ctx).proceed(incoming);
        verify(budget).claim(noBudget.blockPos());
    }

    @Test
    void scriptedNodeForwardsTheEditedTargetsAndState() {
        CastContext ctx = context();
        SpellTarget start = target(1);
        SpellTarget replacement = target(2);
        List<SpellTarget> incoming = List.of(start);
        SpellState changed = ctx.state().with(SpellStats.POWER, 6.0F);
        Consumer<ScriptedSpellEvent> listener = event -> {
            if (event.context() != ctx) {
                return;
            }
            assertEquals("port:script", event.key());
            assertEquals(3.0F, event.power());
            assertEquals(incoming, event.targets());
            event.targets().set(0, replacement);
            event.setState(changed);
        };

        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            script().execute(ctx, incoming);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(List.of(start), incoming);
        verify(ctx).proceed(List.of(replacement), changed);
    }

    @Test
    void stoppedScriptSkipsItsChildren() {
        CastContext ctx = context();
        Consumer<ScriptedSpellEvent> stop = event -> {
            if (event.context() == ctx) {
                event.stop();
            }
        };
        MinecraftForge.EVENT_BUS.addListener(stop);
        try {
            script().execute(ctx, List.of(target(1)));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(stop);
        }
        verify(ctx, never()).proceed(any(), any());
    }

    @Test
    void nativeCastAndNodeEventsExposeMutationsAndCancellation() {
        var caster = mock(LivingEntity.class);
        var spell = Spell.empty();
        var pre = new SpellCastEvent.Pre(caster, ItemStack.EMPTY, spell, 3.0F, 7.0F);
        var original = List.of(target(1));
        var replacement = target(2);
        var node = new SpellNodeEvent(context(), original);
        Consumer<SpellCastEvent.Pre> changeCost = event -> {
            if (event == pre) {
                event.setPower(4.0F);
                event.setVisCost(2.0F);
                event.setCanceled(true);
            }
        };
        Consumer<SpellNodeEvent> changeTargets = event -> {
            if (event == node) {
                event.targets().set(0, replacement);
            }
        };
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(changeCost);
        bus.addListener(changeTargets);
        try {
            assertTrue(bus.post(pre));
            assertFalse(bus.post(node));
        } finally {
            bus.unregister(changeTargets);
            bus.unregister(changeCost);
        }
        assertTrue(pre.isCanceled());
        assertSame(spell, pre.spell());
        assertEquals(4.0F, pre.power());
        assertEquals(2.0F, pre.visCost());
        assertEquals(List.of(replacement), node.targets());
        assertEquals(List.of(target(1).position()), original.stream().map(SpellTarget::position).toList());
    }

    @Test
    void laterInfusionListenersSeeTheReplacedResult() {
        var event = new InfusionCraftedEvent(
                mock(ServerLevel.class), BlockPos.ZERO, null, new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT));
        ItemStack replacement = new ItemStack(Items.DIAMOND, 2);
        Consumer<InfusionCraftedEvent> replace = current -> {
            if (current == event) {
                current.setResultStack(replacement);
            }
        };
        Consumer<InfusionCraftedEvent> changeCount = current -> {
            if (current == event) {
                assertSame(replacement, current.getResultStack());
                current.getResultStack().grow(3);
            }
        };
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventPriority.HIGH, replace);
        bus.addListener(EventPriority.LOW, changeCount);
        try {
            assertFalse(bus.post(event));
        } finally {
            bus.unregister(changeCount);
            bus.unregister(replace);
        }
        assertSame(replacement, event.getResultStack());
        assertEquals(5, event.getResultStack().getCount());
    }

    private static CastContext context() {
        CastContext ctx = mock(CastContext.class);
        when(ctx.state()).thenReturn(SpellState.DEFAULT.with(SpellStats.POWER, 3.0F));
        when(ctx.power()).thenCallRealMethod();
        return ctx;
    }

    private static SpellTarget target(int x) {
        BlockPos pos = new BlockPos(x, 64, 0);
        return SpellTarget.of(new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false), new Vec3(1, 0, 0));
    }

    private static ScriptedNode script() {
        var lookup = new AtomicReference<SpellBehaviorType<ScriptedNode>>();
        var codec = ScriptedNode.codec(lookup::get);
        lookup.set(new SpellBehaviorType<>(SpellPartKind.FLOW, codec));
        var json = new JsonObject();
        json.addProperty("key", "port:script");
        return codec.codec().parse(JsonOps.INSTANCE, json).result().orElseThrow();
    }

    private record Application(SpellTarget target, float power, int index) {}

    private static final class RecordingEffect extends AbstractEffectBehavior {
        private final List<Application> applied = new ArrayList<>();

        @Override
        public SpellBehaviorType<?> type() {
            return new SpellBehaviorType<>(SpellPartKind.EFFECT, MapCodec.unit(this));
        }

        @Override
        protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
            applied.add(new Application(target, power, index));
        }
    }
}
