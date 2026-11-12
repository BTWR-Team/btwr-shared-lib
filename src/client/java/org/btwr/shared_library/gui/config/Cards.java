package org.btwr.shared_library.gui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

/**
 * How one kind of item looks and behaves in a {@link CardGridWidget}.
 */
interface Cards<T> {
    /** The narrowest a card can be before the grid uses fewer columns. */
    int minWidth();

    int maxColumns();

    /** How tall a card is, given how wide the grid made it. */
    int height(int cardWidth);

    int gap();

    void render(DrawContext context, T item, int x, int y, int width, int height, boolean hovered, boolean focused, int mouseX, int mouseY);

    /** Handles a click at a position inside the card. Returns whether it did anything. */
    boolean click(T item, int x, int y, int width, int height, int mouseX, int mouseY, int button);

    /** Handles Enter on the card, where shift reverses a stepper. */
    void activate(T item, boolean shift);

    /** The card's name, for narration. */
    Text name(T item);

    List<Text> tooltip(T item);

    /** A line for the footer while the card is hovered. */
    Text hint(T item);

    boolean matches(T item, String lowerCaseSearch);

    /** The rest of the name that starts with what's typed, as a full string, or null. */
    String completionFor(T item, String typed);

    /** A summary for the footer, or an empty string. */
    String status(List<T> allItems);

    void reset(List<T> allItems);
}