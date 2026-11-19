package com.leclowndu93150.thaumaturge.registry;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

public final class TTSoundTypes {
    public static final SoundType GORE = soundType(TTSounds.GORE, 1.0F);
    public static final SoundType CRYSTAL = soundType(TTSounds.CRYSTAL, 1.0F);
    public static final SoundType JAR = soundType(TTSounds.JAR, 1.0F);
    public static final SoundType URN = soundType(TTSounds.URNBREAK, 1.5F);

    private TTSoundTypes() {}

    private static SoundType soundType(Supplier<SoundEvent> sound, float pitch) {
        return new ForgeSoundType(0.5F, pitch, sound, sound, sound, sound, sound);
    }
}
