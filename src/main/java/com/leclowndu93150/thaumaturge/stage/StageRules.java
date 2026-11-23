package com.leclowndu93150.thaumaturge.stage;

import com.leclowndu93150.thaumaturge.compat.gamestages.GameStagesBridge;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import org.jspecify.annotations.Nullable;

/** Rules belong to the recipe manager prepared for this server resource reload. */
public final class StageRules {
    private final Map<ResourceLocation, List<Rule>> rules = new HashMap<>();

    public static StageRules of(ServerLevel level) {
        return RecipeAccess.of(level.getRecipeManager()).stages();
    }

    public void add(ResourceLocation family, Selector selector, Requirement requirement) {
        rules.computeIfAbsent(family, ignored -> new ArrayList<>()).add(new Rule(selector, requirement));
    }

    public boolean hasRules(ResourceLocation family) {
        return rules.containsKey(family);
    }

    public static @Nullable Requirement denial(ServerLevel level, StageTarget target, @Nullable Player player) {
        List<Rule> candidates = of(level).rules.get(target.family());
        if (candidates == null) {
            return null;
        }
        for (Rule rule : candidates) {
            if (rule.selector.matches(target, level.registryAccess()) && !rule.requirement.satisfied(level, player)) {
                return rule.requirement;
            }
        }
        return null;
    }

    public static boolean allows(ServerLevel level, StageTarget target, @Nullable Player player) {
        return denial(level, target, player) == null;
    }

    public static boolean allowsOwner(ServerLevel level, StageTarget target, @Nullable UUID owner) {
        return !of(level).hasRules(target.family())
                || allows(level, target, owner == null ? null : level.getServer().getPlayerList().getPlayer(owner));
    }

    static @Nullable ServerPlayer verifiedActor(ServerLevel level, @Nullable Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || player instanceof FakePlayer
                || level.getServer().getPlayerList().getPlayer(player.getUUID()) != player) {
            return null;
        }
        return serverPlayer;
    }

    public static boolean check(ServerLevel level, StageTarget target, Player player) {
        Requirement denial = denial(level, target, player);
        if (denial == null) {
            return true;
        }
        denial.tell(player);
        return false;
    }

    private record Rule(Selector selector, Requirement requirement) {}

    @FunctionalInterface
    public interface Selector {
        boolean matches(StageTarget target, RegistryAccess registries);

        static Selector family() {
            return (target, registries) -> true;
        }

        static Selector id(ResourceLocation id) {
            return (target, registries) -> target.id().filter(id::equals).isPresent();
        }

        static Selector entry(ResourceLocation registry, ResourceLocation id) {
            StageTarget.Subject subject = new StageTarget.Subject(registry, id);
            return (target, registries) -> target.subjects().contains(subject);
        }

        static <T> Selector tag(TagKey<T> tag) {
            ResourceKey<? extends Registry<T>> registry = tag.registry();
            return (target, registries) -> {
                var lookup = registries.lookup(registry);
                if (lookup.isEmpty()) {
                    return false;
                }
                return target.subjects().stream()
                        .filter(subject -> registry.location().equals(subject.registry()))
                        .map(subject -> ResourceKey.create(registry, subject.id()))
                        .anyMatch(entry -> lookup.get().get(entry).filter(holder -> holder.is(tag)).isPresent());
            };
        }
    }

    public record Requirement(boolean all, List<String> names) {
        public Requirement {
            names = List.copyOf(names);
            if (names.isEmpty() || names.stream().anyMatch(name -> !name.matches("[a-z0-9_:]+"))) {
                throw new IllegalArgumentException("Expected one or more GameStages names matching [a-z0-9_:]+");
            }
        }

        private boolean satisfied(ServerLevel level, @Nullable Player player) {
            if (!ModList.get().isLoaded("gamestages")) {
                return false;
            }
            ServerPlayer serverPlayer = verifiedActor(level, player);
            if (serverPlayer == null) {
                return false;
            }
            return all
                    ? names.stream().allMatch(name -> GameStagesBridge.hasStage(serverPlayer, name))
                    : names.stream().anyMatch(name -> GameStagesBridge.hasStage(serverPlayer, name));
        }

        public void tell(Player player) {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                TTActionBar.send(player, message().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
        }

        public MutableComponent message() {
            return Component.translatable(all ? "message.thaumaturge.stage.all" : "message.thaumaturge.stage.any",
                    String.join(", ", names));
        }
    }
}
