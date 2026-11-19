package com.leclowndu93150.thaumaturge.content.spell.script;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.ScriptedSpellEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.minecraftforge.common.MinecraftForge;

public final class ScriptedNode implements SpellBehavior {
    private final String key;
    private final int children;
    private final boolean standalone;
    private final Supplier<SpellBehaviorType<?>> type;

    private ScriptedNode(String key, int children, boolean standalone, Supplier<SpellBehaviorType<?>> type) {
        this.key = key;
        this.children = children;
        this.standalone = standalone;
        this.type = type;
    }

    public static MapCodec<ScriptedNode> codec(Supplier<SpellBehaviorType<?>> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                        Codec.STRING.fieldOf("key").forGetter(node -> node.key),
                        Codecs.optionalFieldOf(Codec.intRange(0, 8), "max_children", 1).forGetter(node -> node.children),
                        Codecs.optionalFieldOf(Codec.BOOL, "standalone", false).forGetter(node -> node.standalone))
                .apply(i, (key, children, standalone) -> new ScriptedNode(key, children, standalone, type)));
    }

    @Override
    public SpellBehaviorType<?> type() {
        return type.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        var event = new ScriptedSpellEvent(key, ctx, incoming, ctx.power());
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.stopped()) {
            ctx.proceed(event.targets(), event.state());
        }
    }

    @Override
    public int maxChildren() {
        return children;
    }

    @Override
    public boolean standalone() {
        return standalone;
    }
}
