package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.EldritchStages;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchStructure;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.PortalLink;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class AltarRituals {
    private static final int PARTICLE_INTERVAL = 4;
    private static final int PARTICLE_COUNT = 8;
    private static final double PARTICLE_SPREAD = 0.6;
    private static final double PARTICLE_SPEED = 0.05;
    private static final double PARTICLE_RISE = 1.5;
    private static final int CHANT_INTERVAL = 40;
    private static final int COMPLETION_RETRY_INTERVAL = 20;
    private static final float MIN_DRAIN_FRACTION = 0.5F;

    private AltarRituals() {}

    static void start(ServerLevel level, BlockEntityEldritchAltar altar, ServerPlayer player) {
        if (altar.ritual().isPresent()) {
            return;
        }
        BlockPos pos = altar.getBlockPos();
        var rules = AltarRitualRules.of(level.registryAccess(), altar.site());
        if (!rules.enabled()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_disabled");
            return;
        }
        if (altar.getEyes() < rules.eyes()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.need_eyes", rules.eyes());
            return;
        }
        if (!PlayerResearchAccess.of(player).hasCompleted(TTIds.RESEARCH_OCULUS)) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_unknown");
            return;
        }
        if (linkedMazeActive(level, altar)) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.already_open");
            return;
        }
        if (LabyrinthService.atCapacity(level.getServer())) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.labyrinth_full");
            return;
        }
        if (!level.getBlockState(pos.above()).is(TTBlocks.NODE.get()) && !altar.portalOpen()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.no_node");
            return;
        }
        var definition = chooseDefinition(level, altar);
        if (definition.isEmpty()) {
            TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_disabled");
            return;
        }
        var terms = terms(level, altar, player, rules, definition.get());
        if (!StageRules.check(level, terms.authority().target(), player) || terms.authority().player().isEmpty()) {
            return;
        }
        altar.setRitual(Optional.of(new AltarRitual(player.getUUID(), level.getGameTime(), 0.0F, 0,
                Optional.of(terms), Optional.empty())));
        level.playSound(null, pos, TTSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_begin");
    }

    private static Optional<ResourceKey<LabyrinthDefinition>> chooseDefinition(
            ServerLevel level, BlockEntityEldritchAltar altar) {
        var selection = AltarSite.definition(level, altar)
                .flatMap(site -> site.labyrinths().getRandomValue(level.getRandom())).map(ResourceKey::location);
        return LabyrinthService.definition(level.getServer(), selection).map(holder -> holder.key());
    }

    private static AltarRitual.Terms terms(ServerLevel level, BlockEntityEldritchAltar altar, ServerPlayer caster,
            AltarRitualRules rules, ResourceKey<LabyrinthDefinition> definition) {
        LabyrinthConfig config = ThaumaturgeServerConfig.LABYRINTH;
        StageTarget target = EldritchStages.altar(StageTarget.ELDRITCH_RITUAL, altar).withSubject(definition);
        return new AltarRitual.Terms(definition, StageAuthorization.capture(level, target, caster), rules.eyes(),
                rules.channelTicks().orElseGet(config.ritualChannelTicks::get),
                rules.vis().orElseGet(() -> config.ritualVisCost.get().floatValue()), rules.vis().isPresent());
    }

    static void tick(ServerLevel level, BlockEntityEldritchAltar altar, AltarRitual ritual) {
        LabyrinthConfig config = ThaumaturgeServerConfig.LABYRINTH;
        BlockPos pos = altar.getBlockPos();
        ServerPlayer caster = level.getServer().getPlayerList().getPlayer(ritual.caster());
        if (caster == null) {
            return;
        }
        if (ritual.terms().isEmpty()) {
            var definition = chooseDefinition(level, altar);
            if (definition.isEmpty()) {
                return;
            }
            var terms = terms(level, altar, caster, AltarRitualRules.DEFAULT, definition.get());
            float costPerStep = terms.vis() / terms.duration();
            int paid = costPerStep > 0 ? (int) (ritual.drained() / costPerStep)
                    : (int) Math.min(terms.duration(), Math.max(0, level.getGameTime() - ritual.start()));
            ritual = ritual.withTerms(terms, Math.min(terms.duration(), paid));
            altar.replaceRitual(ritual);
        }
        var terms = ritual.terms().orElseThrow();
        if (!terms.authority().allows(level)) {
            return;
        }
        double reach = config.ritualMaxDistance.get();
        if (!caster.isAlive()
                || caster.level() != level
                || caster.distanceToSqr(Vec3.atCenterOf(pos)) > reach * reach) {
            fizzle(level, altar, caster, "gui.thaumaturge.altar.ritual_broken");
            return;
        }
        if (ritual.steps() >= terms.duration()) {
            if (level.getGameTime() % COMPLETION_RETRY_INTERVAL == 0) {
                complete(level, altar, caster, ritual, terms);
            }
            return;
        }
        float perTick = terms.vis() / terms.duration();
        float due = terms.exactPayment() ? Math.max(0.0F,
                terms.vis() * ((ritual.steps() + 1) / (float) terms.duration()) - ritual.drained()) : perTick;
        if (terms.exactPayment() && due > 0.0F && AuraHelper.drainVis(level, pos, due, true) < due) {
            if (level.getGameTime() % CHANT_INTERVAL == 0) {
                TTActionBar.sendPurple(caster, "gui.thaumaturge.altar.not_enough_vis");
            }
            return;
        }
        float drained = due > 0.0F ? AuraHelper.drainVis(level, pos, due, false) : 0.0F;
        if (!terms.exactPayment() && perTick > 0.0F && drained < perTick * MIN_DRAIN_FRACTION) {
            fizzle(level, altar, caster, "gui.thaumaturge.altar.not_enough_vis");
            return;
        }
        ritual = ritual.drain(drained);
        altar.replaceRitual(ritual);
        int elapsed = ritual.steps();
        if (elapsed % PARTICLE_INTERVAL == 0) {
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5,
                    pos.getY() + PARTICLE_RISE,
                    pos.getZ() + 0.5,
                    PARTICLE_COUNT,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPREAD,
                    PARTICLE_SPEED);
        }
        if (elapsed % CHANT_INTERVAL == 0) {
            level.playSound(null, pos, TTSounds.CHANT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (elapsed >= terms.duration()) {
            complete(level, altar, caster, ritual, terms);
        }
    }

    private static void complete(ServerLevel level, BlockEntityEldritchAltar altar, ServerPlayer caster,
            AltarRitual ritual, AltarRitual.Terms terms) {
        BlockPos pos = altar.getBlockPos();
        Optional<MazeRecord> record = ritual.opened().flatMap(id -> LabyrinthService.byId(level.getServer(), id));
        if (record.isEmpty()) {
            record = LabyrinthService.open(level.getServer(), GlobalPos.of(level.dimension(), pos),
                    Optional.of(terms.definition().location()));
            if (record.isPresent()) {
                altar.replaceRitual(ritual.withOpened(record.get().plan().id()));
            }
        }
        if (record.isEmpty()) {
            if (level.getGameTime() % CHANT_INTERVAL == 0) {
                TTActionBar.sendPurple(caster, "gui.thaumaturge.altar.labyrinth_full");
            }
            return;
        }
        var id = record.get().plan().id();
        var portal = EldritchPlacement.place(level, pos.above(), TTBlocks.ELDRITCH_PORTAL.get().defaultBlockState(),
                TTBlockEntities.ELDRITCH_PORTAL.get());
        if (portal.isEmpty()) {
            return;
        }
        portal.get().setLink(PortalLink.intoLabyrinth(id));
        altar.setRitual(Optional.empty());
        altar.setEyes(altar.getEyes() - terms.eyes());
        altar.link().ifPresent(previous -> LabyrinthService.unlink(level.getServer(), previous));
        altar.setLink(Optional.of(id));
        if (ThaumaturgeServerConfig.LABYRINTH.sealSiteWhileLinked.get()) {
            SiteSealing.apply(level, pos, true);
        }
        altar.setGarrison(altar.garrison().quell());
        level.playSound(null, pos, TTSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        TTActionBar.sendPurple(caster, "gui.thaumaturge.altar.ritual_complete");
    }

    static void updateSeal(ServerLevel level, BlockEntityEldritchAltar altar) {
        boolean sealed = ThaumaturgeServerConfig.LABYRINTH.sealSiteWhileLinked.get() && linkedMazeActive(level, altar);
        BlockState state = altar.getBlockState();
        if (state.hasProperty(BlockEldritchStructure.SEALED)
                && state.getValue(BlockEldritchStructure.SEALED) != sealed) {
            SiteSealing.apply(level, altar.getBlockPos(), sealed);
        }
    }

    private static void fizzle(
            ServerLevel level, BlockEntityEldritchAltar altar, @Nullable ServerPlayer caster, String message) {
        altar.setRitual(Optional.empty());
        level.playSound(null, altar.getBlockPos(), TTSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (caster != null) {
            TTActionBar.sendPurple(caster, message);
        }
    }

    private static boolean linkedMazeActive(ServerLevel level, BlockEntityEldritchAltar altar) {
        return altar.portalOpen()
                && altar.link()
                        .flatMap(id -> LabyrinthService.byId(level.getServer(), id))
                        .map(record -> record.state().phase())
                        .filter(phase -> phase != LabyrinthPhase.CONQUERED && phase != LabyrinthPhase.RETIRED)
                        .isPresent();
    }
}
