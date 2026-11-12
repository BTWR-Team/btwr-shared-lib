package org.btwr.shared_library.gui.config;

import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * A searchable set of IDs to tick, such as mobs or damage types. Items load when their page first
 * opens, so implementations can do slow work in {@link #items()} as long as they keep the result.
 */
public interface Checklist {
    record Entry(String id, String name, ItemStack icon) {}

    List<Entry> items();

    boolean isChecked(String id);

    void setChecked(String id, boolean checked);

    /** Why this item can't be changed here, or null if it can. A locked item shows as checked. */
    default Text lockReason(String id) {
        return null;
    }

    /** Unticks everything that can be changed. Used by the Reset button. */
    default void clear() {
        for (Entry item : this.items()) {
            if (this.lockReason(item.id()) == null && this.isChecked(item.id())) {
                this.setChecked(item.id(), false);
            }
        }
    }
}