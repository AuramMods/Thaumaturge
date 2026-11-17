package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, TTIds.MODID);

    public static final RegistryObject<SoundEvent> PAGE = register("page");
    public static final RegistryObject<SoundEvent> PAGETURN = register("pageturn");
    public static final RegistryObject<SoundEvent> CLACK = register("clack");
    public static final RegistryObject<SoundEvent> WRITE = register("write");
    public static final RegistryObject<SoundEvent> JAR = register("jar");
    public static final RegistryObject<SoundEvent> CREAK = register("creak");
    public static final RegistryObject<SoundEvent> KEY = register("key");
    public static final RegistryObject<SoundEvent> PUMP = register("pump");
    public static final RegistryObject<SoundEvent> SQUEEK = register("squeek");
    public static final RegistryObject<SoundEvent> TOOL = register("tool");

    public static final RegistryObject<SoundEvent> BRAIN = register("brain");
    public static final RegistryObject<SoundEvent> BUBBLE = register("bubble");
    public static final RegistryObject<SoundEvent> CHANT = register("chant");
    public static final RegistryObject<SoundEvent> COINS = register("coins");
    public static final RegistryObject<SoundEvent> CRAFTFAIL = register("craftfail");
    public static final RegistryObject<SoundEvent> CRAFTSTART = register("craftstart");
    public static final RegistryObject<SoundEvent> CRYSTAL = register("crystal");
    public static final RegistryObject<SoundEvent> DUST = register("dust");
    public static final RegistryObject<SoundEvent> ERASE = register("erase");
    public static final RegistryObject<SoundEvent> EVILPORTAL = register("evilportal");
    public static final RegistryObject<SoundEvent> FLY = register("fly");
    public static final RegistryObject<SoundEvent> GORE = register("gore");
    public static final RegistryObject<SoundEvent> GRIND = register("grind");
    public static final RegistryObject<SoundEvent> HEARTBEAT = register("heartbeat");
    public static final RegistryObject<SoundEvent> HHOFF = register("hhoff");
    public static final RegistryObject<SoundEvent> HHON = register("hhon");
    public static final RegistryObject<SoundEvent> ICE = register("ice");
    public static final RegistryObject<SoundEvent> INFUSER = register("infuser");
    public static final RegistryObject<SoundEvent> INFUSERSTART = register("infuserstart");
    public static final RegistryObject<SoundEvent> JACOBS = register("jacobs");
    public static final RegistryObject<SoundEvent> LEARN = register("learn");
    public static final RegistryObject<SoundEvent> MONOLITH = register("monolith");
    public static final RegistryObject<SoundEvent> POOF = register("poof");
    public static final RegistryObject<SoundEvent> RUMBLE = register("rumble");
    public static final RegistryObject<SoundEvent> RUNICSHIELDCHARGE = register("runicshieldcharge");
    public static final RegistryObject<SoundEvent> RUNICSHIELDEFFECT = register("runicshieldeffect");
    public static final RegistryObject<SoundEvent> SCAN = register("scan");
    public static final RegistryObject<SoundEvent> SHOCK = register("shock");
    public static final RegistryObject<SoundEvent> SPILL = register("spill");
    public static final RegistryObject<SoundEvent> TICKS = register("ticks");
    public static final RegistryObject<SoundEvent> CAMERA_TICKS = register("cameraticks");
    public static final RegistryObject<SoundEvent> UPGRADE = register("upgrade");
    public static final RegistryObject<SoundEvent> URNBREAK = register("urnbreak");
    public static final RegistryObject<SoundEvent> WAND = register("wand");
    public static final RegistryObject<SoundEvent> WANDFAIL = register("wandfail");
    public static final RegistryObject<SoundEvent> WHISPERS = register("whispers");
    public static final RegistryObject<SoundEvent> WIND = register("wind");
    public static final RegistryObject<SoundEvent> ZAP = register("zap");

    public static final RegistryObject<SoundEvent> SWARM = register("swarm");
    public static final RegistryObject<SoundEvent> SWARMATTACK = register("swarmattack");
    public static final RegistryObject<SoundEvent> WISPDEAD = register("wispdead");
    public static final RegistryObject<SoundEvent> WISPLIVE = register("wisplive");
    public static final RegistryObject<SoundEvent> PECH_IDLE = register("pech_idle");
    public static final RegistryObject<SoundEvent> PECH_TRADE = register("pech_trade");
    public static final RegistryObject<SoundEvent> PECH_DICE = register("pech_dice");
    public static final RegistryObject<SoundEvent> PECH_HIT = register("pech_hit");
    public static final RegistryObject<SoundEvent> PECH_DEATH = register("pech_death");
    public static final RegistryObject<SoundEvent> PECH_CHARGE = register("pech_charge");
    public static final RegistryObject<SoundEvent> TENTACLE = register("tentacle");
    public static final RegistryObject<SoundEvent> EGIDLE = register("egidle");
    public static final RegistryObject<SoundEvent> EGATTACK = register("egattack");
    public static final RegistryObject<SoundEvent> EGDEATH = register("egdeath");
    public static final RegistryObject<SoundEvent> EGSCREECH = register("egscreech");
    public static final RegistryObject<SoundEvent> CRABCLAW = register("crabclaw");
    public static final RegistryObject<SoundEvent> CRABDEATH = register("crabdeath");
    public static final RegistryObject<SoundEvent> CRABTALK = register("crabtalk");

    private TTSounds() {}

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(
                name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
