package org.btwr.shared_library.gui.config;

import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * Describes a mod's settings. This is all a mod writes: {@link ConfigUiScreen} draws it, so no mod
 * needs a screen class of its own. {@code save} runs when the screen closes.
 */
public record ConfigUiSpec(Text title, Runnable save, List<Category> categories) {
    public ConfigUiSpec {
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("A config screen needs at least one category");
        }
    }

    /** An entry in the left-hand rail. Its sections show as pills, unless there's only one. */
    public record Category(Text name, ItemStack icon, List<Section> sections) {
        public Category {
            if (sections.isEmpty()) {
                throw new IllegalArgumentException("A category needs at least one section");
            }
        }
    }

    /**
     * A page of content: either setting cards or a checklist.
     *
     * @param name        the section's pill label
     * @param description a line above the content, or null
     * @param note        a short note for the footer, re-read every frame, which may return null
     */
    public record Section(Text name, Text description, Supplier<Text> note, List<UiSetting<?>> settings, Checklist checklist) {
        public static Section settings(Text name, Text description, List<UiSetting<?>> settings) {
            return new Section(name, description, () -> null, settings, null);
        }

        public static Section checklist(Text name, Text description, Supplier<Text> note, Checklist checklist) {
            return new Section(name, description, note, null, checklist);
        }
    }
}