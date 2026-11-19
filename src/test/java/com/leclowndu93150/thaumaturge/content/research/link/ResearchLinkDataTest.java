package com.leclowndu93150.thaumaturge.content.research.link;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class ResearchLinkDataTest {
    @Test
    void sharedProgressOnlyMovesForward() {
        ResearchLinkData.Progress complete = new ResearchLinkData.Progress(2, true);

        assertEquals(complete, new ResearchLinkData.Progress(1, false).merge(complete));
        assertEquals(complete, complete.merge(new ResearchLinkData.Progress(1, false)));
        assertEquals(new ResearchLinkData.Progress(3, true), complete.merge(new ResearchLinkData.Progress(3, false)));
    }

    @Test
    void progressSurvivesSavedDataRoundTrip() {
        ResourceLocation research = ResourceLocation.parse("thaumaturge:test");
        ResearchLinkData.Link link = new ResearchLinkData.Link(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Set.of(),
                Map.of(research, new ResearchLinkData.Progress(2, false)));

        var encoded = Codecs.getOrThrow(ResearchLinkData.Link.CODEC.encodeStart(NbtOps.INSTANCE, link));
        ResearchLinkData.Link decoded = Codecs.getOrThrow(ResearchLinkData.Link.CODEC.parse(NbtOps.INSTANCE, encoded));

        assertEquals(link.progress(), decoded.progress());
    }
}
