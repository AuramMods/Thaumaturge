package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.slf4j.Logger;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class FocalSpriteRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEXTURE_ROOT = "textures/gui/sprites";
    private static final FileToIdConverter SPRITES = new FileToIdConverter(TEXTURE_ROOT, ".png");
    private static final MetadataSectionSerializer<Scaling> METADATA = new MetadataSectionSerializer<>() {
        @Override
        public String getMetadataSectionName() {
            return "gui";
        }

        @Override
        public Scaling fromJson(JsonObject metadata) {
            JsonObject scaling = GsonHelper.getAsJsonObject(metadata, "scaling", new JsonObject());
            return switch (GsonHelper.getAsString(scaling, "type", "stretch")) {
                case "stretch" -> Stretch.INSTANCE;
                case "tile" -> new Tile(extent(scaling, "width"), extent(scaling, "height"));
                case "nine_slice" -> new NineSlice(extent(scaling, "width"), extent(scaling, "height"), Border.parse(scaling));
                default -> throw new JsonParseException("Unrecognized GUI sprite scaling " + scaling);
            };
        }
    };
    private static volatile Map<ResourceLocation, Scaling> scalingByTexture = Map.of();

    private FocalSpriteRenderer() {}

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) FocalSpriteRenderer::reload);
    }

    static void reload(ResourceManager resources) {
        var loaded = new HashMap<ResourceLocation, Scaling>();
        resources.listResources(TEXTURE_ROOT + "/focal", id -> id.getPath().endsWith(".png"))
                .forEach((texture, resource) -> {
                    try {
                        resource.metadata().getSection(METADATA).ifPresent(scaling -> loaded.put(texture, scaling));
                    } catch (IOException | JsonParseException exception) {
                        LOGGER.warn("Could not read GUI scaling for {} from {}", texture, resource.sourcePackId(), exception);
                    }
                });
        scalingByTexture = Map.copyOf(loaded);
    }

    static void blit(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        ResourceLocation texture = SPRITES.idToFile(sprite);
        scalingByTexture.getOrDefault(texture, Stretch.INSTANCE).blit(graphics, texture, x, y, width, height);
    }

    private static int extent(JsonObject scaling, String name) {
        int value = GsonHelper.getAsInt(scaling, name);
        if (value <= 0) {
            throw new JsonParseException("GUI sprite " + name + " must be positive");
        }
        return value;
    }

    private interface Scaling {
        void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height);
    }

    private enum Stretch implements Scaling {
        INSTANCE;

        @Override
        public void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
            graphics.blit(texture, x, y, width, height, 0.0F, 0.0F, width, height, width, height);
        }
    }

    private record Tile(int width, int height) implements Scaling {
        @Override
        public void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
            tile(graphics, texture, x, y, width, height, 0, 0, this.width, this.height, this.width, this.height);
        }
    }

    private record NineSlice(int width, int height, Border border) implements Scaling {
        private NineSlice {
            if ((long) border.left + border.right >= width || (long) border.top + border.bottom >= height) {
                throw new JsonParseException("GUI sprite borders must leave a positive center");
            }
        }

        @Override
        public void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
            if (width == this.width && height == this.height) {
                Stretch.INSTANCE.blit(graphics, texture, x, y, width, height);
                return;
            }
            int left = Math.min(border.left, width / 2);
            int right = Math.min(border.right, width / 2);
            int top = Math.min(border.top, height / 2);
            int bottom = Math.min(border.bottom, height / 2);
            int[] columns = {0, left, width - right, width};
            int[] rows = {0, top, height - bottom, height};
            int[] sourceColumns = {0, left, this.width - right, this.width};
            int[] sourceRows = {0, top, this.height - bottom, this.height};
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    tile(graphics, texture, x + columns[column], y + rows[row],
                            columns[column + 1] - columns[column], rows[row + 1] - rows[row],
                            sourceColumns[column], sourceRows[row],
                            sourceColumns[column + 1] - sourceColumns[column], sourceRows[row + 1] - sourceRows[row],
                            this.width, this.height);
                }
            }
        }
    }

    private record Border(int left, int top, int right, int bottom) {
        private Border {
            if (left < 0 || top < 0 || right < 0 || bottom < 0) {
                throw new JsonParseException("GUI sprite borders must be nonnegative");
            }
        }

        static Border parse(JsonObject scaling) {
            if (GsonHelper.isNumberValue(scaling, "border")) {
                int size = GsonHelper.getAsInt(scaling, "border");
                return new Border(size, size, size, size);
            }
            JsonObject sides = GsonHelper.getAsJsonObject(scaling, "border");
            return new Border(GsonHelper.getAsInt(sides, "left"), GsonHelper.getAsInt(sides, "top"),
                    GsonHelper.getAsInt(sides, "right"), GsonHelper.getAsInt(sides, "bottom"));
        }
    }

    private static void tile(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
            int u, int v, int tileWidth, int tileHeight, int textureWidth, int textureHeight) {
        for (int dx = 0; dx < width; ) {
            int w = Math.min(tileWidth, width - dx);
            for (int dy = 0; dy < height; ) {
                int h = Math.min(tileHeight, height - dy);
                graphics.blit(texture, x + dx, y + dy, w, h, (float) u, (float) v, w, h, textureWidth, textureHeight);
                dy += h;
            }
            dx += w;
        }
    }
}
