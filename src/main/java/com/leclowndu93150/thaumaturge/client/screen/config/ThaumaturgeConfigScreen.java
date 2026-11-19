package com.leclowndu93150.thaumaturge.client.screen.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingException;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.donator.DonatorCapeClient;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

public final class ThaumaturgeConfigScreen extends Screen {
    private static final String PREFIX = TTIds.MODID + ".configuration.";
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int ERROR_COLOR = 0xFF6666;
    private final Screen parent;

    public ThaumaturgeConfigScreen(Screen parent) {
        super(message("title", "Thaumaturge Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = 48;
        int buttonWidth = Math.min(240, width - 32);
        for (ModConfig.Type type : ModConfig.Type.values()) {
            List<ModConfig> configs = ConfigTracker.INSTANCE.configSets().get(type).stream()
                    .filter(config -> TTIds.MODID.equals(config.getModId()))
                    .sorted(Comparator.comparing(ModConfig::getFileName)).toList();
            for (ModConfig config : configs) {
                Button button = Button.builder(fileTitle(config, false), pressed ->
                                minecraft.setScreen(new FileScreen(this, config)))
                        .bounds((width - buttonWidth) / 2, y, buttonWidth, 20).build();
                button.active = canEdit(minecraft, config);
                Component hint = Component.literal(config.getFileName());
                if (!button.active) {
                    hint = config.getType() == ModConfig.Type.SERVER
                            ? message("server_local_only", "Open a private singleplayer world to edit server settings.")
                            : message("not_loaded", "This config file has not loaded.");
                }
                button.setTooltip(Tooltip.create(hint));
                addRenderableWidget(button);
                y += 24;
            }
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, pressed -> onClose())
                .bounds(width / 2 - 80, height - 28, 160, 20).build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 21, TEXT_COLOR);
    }

    static boolean canEdit(Minecraft game, ModConfig config) {
        var server = game.getSingleplayerServer();
        return config.getSpec() instanceof ForgeConfigSpec spec && spec.isLoaded()
                && config.getConfigData() instanceof CommentedFileConfig
                && (config.getType() != ModConfig.Type.SERVER || (server != null && !server.isPublished()));
    }

    private static Component fileTitle(ModConfig config, boolean title) {
        String path = config.getFileName().replaceAll("[^a-zA-Z0-9]+", ".").toLowerCase(Locale.ROOT);
        String type = readable(config.getType().name().toLowerCase(Locale.ROOT)) + " Settings";
        return message("section." + path + (title ? ".title" : ""), title ? "Thaumaturge " + type : type);
    }

    private static Component message(String key, String fallback) {
        return Component.translatableWithFallback(PREFIX + key, fallback);
    }

    private static Component label(String key) {
        return message(key, readable(key));
    }

    private static String readable(String key) {
        String words = key.replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    private static final class FileScreen extends Screen {
        private final Screen parent;
        private final ModConfig config;
        private final ConfigDraft draft;
        private Button done;
        private boolean saveFailed;

        FileScreen(Screen parent, ModConfig config) {
            super(fileTitle(config, true));
            this.parent = parent;
            this.config = config;
            this.draft = new ConfigDraft((ForgeConfigSpec) config.<ForgeConfigSpec>getSpec(),
                    config.getType() == ModConfig.Type.CLIENT && DonatorCapeClient.isLocalDonator());
        }

        @Override
        protected void init() {
            ConfigList list = new ConfigList(minecraft, width, height);
            addRenderableWidget(list);
            done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, pressed -> apply())
                    .bounds(width / 2 - 134, height - 28, 130, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, pressed -> onClose())
                    .bounds(width / 2 + 4, height - 28, 130, 20).build());
            List<String> lastSection = List.of();
            for (ConfigDraft.Setting setting : draft.settings()) {
                List<String> path = setting.path();
                List<String> section = path.subList(0, path.size() - 1);
                if (!section.equals(lastSection) && !section.isEmpty()) {
                    list.add(new SectionRow(font, sectionLabel(section)));
                }
                lastSection = section;
                list.add(new SettingRow(font, label(path.get(path.size() - 1)), setting, this::updateValidation));
            }
            updateValidation();
        }

        private static Component sectionLabel(List<String> section) {
            var name = Component.empty();
            for (String segment : section) {
                if (!name.getSiblings().isEmpty()) name.append(" / ");
                name.append(label(segment));
            }
            return name;
        }

        private void updateValidation() {
            done.active = draft.valid() && canEdit(minecraft, config);
        }

        private void apply() {
            if (!draft.valid() || !canEdit(minecraft, config)) {
                updateValidation();
                return;
            }
            try {
                draft.apply((CommentedFileConfig) config.getConfigData());
                onClose();
            } catch (WritingException failure) {
                LogUtils.getLogger().warn("Unable to write {}", config.getFileName(), failure);
                saveFailed = true;
            }
        }

        @Override
        public void onClose() {
            minecraft.setScreen(parent);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics);
            super.render(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(font, title, width / 2, 17, TEXT_COLOR);
            if (saveFailed) {
                graphics.drawCenteredString(font, message("save_failed", "Could not save settings."),
                        width / 2, height - 43, ERROR_COLOR);
            }
        }
    }

    private static final class ConfigList extends ContainerObjectSelectionList<Row> {
        ConfigList(Minecraft game, int width, int height) {
            super(game, width, height, 34, height - 50, 25);
            setRenderBackground(false);
        }

        void add(Row row) { addEntry(row); }

        @Override
        public int getRowWidth() {
            return Math.min(540, width - 32);
        }

        @Override
        protected int getScrollbarPosition() {
            return width / 2 + getRowWidth() / 2 + 5;
        }
    }

    private abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {}

    private static final class SectionRow extends Row {
        private final Font font;
        private final Component title;

        SectionRow(Font font, Component title) {
            this.font = font;
            this.title = title;
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(); }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.drawString(font, title, left, top + 8, 0xE6CE97);
        }
    }

    private static final class SettingRow extends Row {
        private static final int RESET_WIDTH = 50;
        private final Font font;
        private final Component name;
        private final AbstractWidget widget;
        private final Button reset;

        SettingRow(Font font, Component name, ConfigDraft.Setting setting, Runnable validate) {
            this.font = font;
            this.name = name;
            Control control = makeControl(font, name, setting, validate);
            this.widget = control.widget();
            var hint = Component.empty().append(name);
            String comment = setting.spec().getComment();
            if (comment != null) hint.append("\n").append(comment);
            if (setting.spec().getDefault() instanceof List<?>) {
                hint.append("\n").append(message("list_hint", "Separate entries with commas."));
            }
            widget.setTooltip(Tooltip.create(hint));
            reset = Button.builder(Component.translatable("controls.reset"), pressed -> {
                setting.select(setting.spec().getDefault());
                control.refresh().run();
                validate.run();
            }).size(RESET_WIDTH, 20).build();
            reset.setTooltip(Tooltip.create(name));
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(widget, reset); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(widget, reset); }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovered, float partialTick) {
            int controlWidth = (width - RESET_WIDTH - 16) / 2;
            int labelWidth = width - RESET_WIDTH - controlWidth - 16;
            graphics.drawString(font, font.plainSubstrByWidth(name.getString(), labelWidth),
                    left, top + 6, TEXT_COLOR);
            widget.setWidth(controlWidth);
            widget.setPosition(left + labelWidth + 8, top);
            reset.setPosition(left + width - RESET_WIDTH, top);
            widget.render(graphics, mouseX, mouseY, partialTick);
            reset.render(graphics, mouseX, mouseY, partialTick);
        }

        private static Control makeControl(Font font, Component name, ConfigDraft.Setting setting, Runnable validate) {
            Object defaultValue = setting.spec().getDefault();
            if (defaultValue instanceof Boolean) {
                var control = CycleButton.onOffBuilder((Boolean) setting.value()).displayOnlyValue()
                        .create(0, 0, 100, 20, name, (button, value) -> setting.select(value));
                return new Control(control, () -> control.setValue((Boolean) setting.value()));
            }
            if (defaultValue instanceof Enum<?> enumeration) {
                List<Enum<?>> values = Arrays.<Enum<?>>stream(enumeration.getDeclaringClass().getEnumConstants())
                        .filter(setting.spec()::test).toList();
                var control = CycleButton.<Enum<?>>builder(value ->
                                Component.literal(readable(value.name().toLowerCase(Locale.ROOT))))
                        .withValues(values).withInitialValue((Enum<?>) setting.value()).displayOnlyValue()
                        .create(0, 0, 100, 20, name, (button, value) -> setting.select(value));
                return new Control(control, () -> control.setValue((Enum<?>) setting.value()));
            }
            var field = new EditBox(font, 0, 0, 100, 20, name);
            field.setMaxLength(Integer.MAX_VALUE);
            field.setValue(setting.text());
            field.setTextColor(setting.valid() ? TEXT_COLOR : ERROR_COLOR);
            field.setResponder(text -> {
                setting.edit(text);
                field.setTextColor(setting.valid() ? TEXT_COLOR : ERROR_COLOR);
                validate.run();
            });
            return new Control(field, () -> field.setValue(setting.text()));
        }

        private record Control(AbstractWidget widget, Runnable refresh) {}
    }
}
