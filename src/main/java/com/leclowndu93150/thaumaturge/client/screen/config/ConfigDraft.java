package com.leclowndu93150.thaumaturge.client.screen.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jspecify.annotations.Nullable;

/** Holds edits until the player applies the current config file. */
final class ConfigDraft {
    private final ForgeConfigSpec spec;
    private final List<Setting> settings = new ArrayList<>();

    ConfigDraft(ForgeConfigSpec spec, boolean donor) {
        this.spec = spec;
        collect(spec.getValues(), donor);
    }

    private void collect(UnmodifiableConfig section, boolean donor) {
        for (var entry : section.entrySet()) {
            if (entry.getRawValue() instanceof UnmodifiableConfig nested) {
                collect(nested, donor);
            } else if (entry.getRawValue() instanceof ForgeConfigSpec.ConfigValue<?> value) {
                List<String> path = value.getPath();
                if (donor || spec != ThaumaturgeClientConfig.SPEC
                        || !path.equals(List.of(ThaumaturgeClientConfig.DONATOR_CAPE_KEY))) {
                    settings.add(new Setting(path, spec.getSpec().get(path), value.get()));
                }
            }
        }
    }

    List<Setting> settings() {
        return List.copyOf(settings);
    }

    boolean valid() {
        return settings.stream().allMatch(Setting::valid);
    }

    void apply(CommentedFileConfig file) {
        if (!valid()) {
            throw new IllegalStateException("Invalid configuration changes");
        }
        List<Setting> changed = settings.stream().filter(Setting::changed).toList();
        if (changed.isEmpty()) {
            return;
        }

        // Forge opens this file with autosave enabled. Publish the changed sections in one update.
        CommentedConfig previous = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
        for (Setting setting : changed) {
            List<String> root = setting.path().subList(0, 1);
            previous.add(root, file.getRaw(root));
        }
        CommentedConfig updated = copySections(previous);
        for (Setting setting : changed) {
            updated.set(setting.path(), setting.value());
        }
        try {
            file.putAll(updated);
        } catch (WritingException failure) {
            try {
                file.putAll(previous);
            } catch (WritingException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
        spec.afterReload();
    }

    private static CommentedConfig copySections(UnmodifiableCommentedConfig source) {
        CommentedConfig copy = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
        for (var entry : source.entrySet()) {
            Object value = entry.getRawValue();
            if (value instanceof UnmodifiableCommentedConfig section) {
                value = copySections(section);
            }
            copy.set(List.of(entry.getKey()), value);
        }
        copy.commentMap().putAll(source.commentMap());
        return copy;
    }

    static final class Setting {
        private final List<String> path;
        private final ForgeConfigSpec.ValueSpec spec;
        private final Object original;
        private @Nullable Object value;
        private String text;

        Setting(List<String> path, ForgeConfigSpec.ValueSpec spec, Object original) {
            this.path = path;
            this.spec = spec;
            this.original = original;
            select(original);
        }

        List<String> path() { return path; }
        ForgeConfigSpec.ValueSpec spec() { return spec; }
        @Nullable Object value() { return value; }
        String text() { return text; }
        boolean valid() { return value != null; }
        boolean changed() { return !Objects.equals(original, value); }

        void select(Object selected) {
            value = selected;
            text = selected instanceof List<?> list ? String.join(", ", list.stream().map(Object::toString).toList())
                    : selected.toString();
        }

        void edit(String input) {
            text = input;
            Object parsed = parse(input, spec.getDefault());
            value = parsed != null && spec.test(parsed) ? parsed : null;
        }

        private static @Nullable Object parse(String input, Object defaultValue) {
            String trimmed = input.strip();
            try {
                if (defaultValue instanceof Integer) return Integer.valueOf(trimmed);
                if (defaultValue instanceof Long) return Long.valueOf(trimmed);
                if (defaultValue instanceof Double) {
                    double number = Double.parseDouble(trimmed);
                    return Double.isFinite(number) ? number : null;
                }
                if (defaultValue instanceof List<?>) {
                    return trimmed.isEmpty() ? List.of()
                            : Arrays.stream(trimmed.split(",", -1)).map(String::strip).toList();
                }
                return input;
            } catch (NumberFormatException invalidNumber) {
                return null;
            }
        }
    }
}
