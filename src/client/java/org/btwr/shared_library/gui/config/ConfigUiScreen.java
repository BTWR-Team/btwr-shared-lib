package org.btwr.shared_library.gui.config;

import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The one screen every mod's settings share. It draws a {@link ConfigUiSpec}, so a mod only describes
 * its settings: {@code new ConfigUiScreen(parent, spec)}. The window fills the game window and the
 * cards reflow as it grows.
 */
public final class ConfigUiScreen extends Screen {
    private static final int BAR_HEIGHT = 20;
    private static final int TITLE_INSET = 8;
    private static final int FOOTER_HEIGHT = 22;
    private static final int RAIL_MIN = 90;
    private static final int RAIL_MAX = 150;
    private static final int PILL_HEIGHT = 15;
    private static final int PILL_GAP = 4;
    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 15;
    private static final long FLASH_MS = 2500L;
    private static final long RESET_CONFIRM_MS = 3000L;

    private final Screen parent;
    private final ConfigUiSpec spec;
    /** The section open in each category, so going back to a category returns to the same place. */
    private final int[] openSections;

    private int category;
    private String query = "";
    private boolean rebuildPending;

    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private int railWidth;
    private int footerY;
    private int contentX;
    private int contentWidth;
    private int descriptionY;

    private SearchField search;
    private CardGridWidget<?> grid;
    private Supplier<String> status = () -> "";
    private Runnable resetAction = () -> {
    };
    private Runnable applyFilter = () -> {
    };
    private FlatButton resetButton;
    private long resetArmedUntil;
    private Text flashMessage;
    private long flashUntil;
    private TextFieldWidget textEditor;
    private UiSetting<String> editedSetting;
    private boolean focusTextEditor;

    public ConfigUiScreen(Screen parent, ConfigUiSpec spec) {
        super(spec.title());
        this.parent = parent;
        this.spec = spec;
        this.openSections = new int[spec.categories().size()];
    }

    private ConfigUiSpec.Category currentCategory() {
        return this.spec.categories().get(this.category);
    }

    private ConfigUiSpec.Section currentSection() {
        return this.currentCategory().sections().get(this.openSections[this.category]);
    }

    @Override
    protected void init() {
        this.commitTextEditor();
        this.resetArmedUntil = 0L;
        this.computeLayout();

        this.addSearch();
        this.addRail();
        int contentY = this.addSectionPills();

        ConfigUiSpec.Section section = this.currentSection();
        this.descriptionY = contentY;
        if (section.description() != null) {
            contentY += 13;
        }

        int gridHeight = this.footerY - 2 - contentY;
        if (section.checklist() != null) {
            this.addGrid(new ChecklistCards(this, section.checklist()), section.checklist().items(), contentY, gridHeight);
        } else {
            this.addGrid(new SettingCards(this, section.settings()), section.settings(), contentY, gridHeight);
        }

        this.addFooterButtons();
    }

    /** Sizes everything from the game window, so the screen grows with it. */
    private void computeLayout() {
        int marginX = Math.max(10, this.width / 24);
        int marginY = Math.max(8, this.height / 24);
        this.windowX = marginX;
        this.windowY = marginY;
        this.windowWidth = this.width - marginX * 2;
        this.windowHeight = this.height - marginY * 2;
        this.railWidth = Math.min(RAIL_MAX, Math.max(RAIL_MIN, this.windowWidth * 22 / 100));
        this.footerY = this.windowY + this.windowHeight - FOOTER_HEIGHT;
        this.contentX = this.windowX + this.railWidth + 8;
        this.contentWidth = this.windowWidth - this.railWidth - 16 - Scroller.WIDTH - 3;
    }

    private void addSearch() {
        int width = Math.min(130, this.windowWidth / 3);
        this.search = this.addDrawableChild(new SearchField(this.textRenderer, this.windowX + this.windowWidth - width - 4, this.windowY + 3, width, 14,
                Text.translatable("screen.config_ui.search"), this.query));
        this.search.onChange(value -> {
            this.query = value;
            this.applyFilter.run();
        });
    }

    private void addRail() {
        List<ConfigUiSpec.Category> categories = this.spec.categories();
        int y = this.windowY + BAR_HEIGHT + 5;
        for (int i = 0; i < categories.size(); i++) {
            int index = i;
            ConfigUiSpec.Category entry = categories.get(i);
            this.addDrawableChild(new RailButton(this.windowX, y, this.railWidth, entry.name(), entry.icon(), i == this.category, () -> this.openCategory(index)));
            y += RailButton.HEIGHT + 2;
        }
    }

    /** Adds the section pills, wrapping to more lines if needed. Returns where the content starts. */
    private int addSectionPills() {
        int y = this.windowY + BAR_HEIGHT + 7;
        List<ConfigUiSpec.Section> sections = this.currentCategory().sections();
        if (sections.size() < 2) {
            return y;
        }

        int x = this.contentX;
        int open = this.openSections[this.category];
        for (int i = 0; i < sections.size(); i++) {
            int index = i;
            Text name = sections.get(i).name();
            int width = Math.min(this.textRenderer.getWidth(name) + 12, this.contentWidth);
            if (x > this.contentX && x + width > this.contentX + this.contentWidth) {
                x = this.contentX;
                y += PILL_HEIGHT + PILL_GAP;
            }
            this.addDrawableChild(new FlatButton(x, y, width, PILL_HEIGHT, name, i == open, () -> this.openSection(index)));
            x += width + PILL_GAP;
        }
        return y + PILL_HEIGHT + 5;
    }

    private <T> void addGrid(Cards<T> cards, List<T> items, int y, int height) {
        Scroller scroller = new Scroller(this.contentX + this.contentWidth + 3, y, height);
        CardGridWidget<T> widget = new CardGridWidget<>(this, cards, this.contentX, y, this.contentWidth, height, scroller);
        this.grid = this.addDrawableChild(widget);
        this.addDrawableChild(scroller);

        this.status = () -> cards.status(items);
        this.resetAction = () -> cards.reset(items);
        this.applyFilter = () -> {
            String search = this.query.trim().toLowerCase(Locale.ROOT);
            widget.setItems(items.stream().filter(item -> cards.matches(item, search)).toList());
        };
        this.applyFilter.run();

        Function<String, String> completer = typed -> {
            String search = typed.trim().toLowerCase(Locale.ROOT);
            for (T item : items) {
                if (cards.matches(item, search)) {
                    String completion = cards.completionFor(item, typed);
                    if (completion != null) {
                        return completion;
                    }
                }
            }
            return null;
        };

        this.search.setCompleter(completer);
    }

    private void addFooterButtons() {
        int y = this.footerY + 4;
        int right = this.windowX + this.windowWidth - 6;
        this.resetButton = this.addDrawableChild(new FlatButton(right - BUTTON_WIDTH * 2 - 6, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.translatable("screen.config_ui.reset"), false, this::pressReset));
        this.resetButton.setTooltip(Tooltip.of(Text.translatable("screen.config_ui.reset.tooltip")));
        this.addDrawableChild(new FlatButton(right - BUTTON_WIDTH, y, BUTTON_WIDTH, BUTTON_HEIGHT, ScreenTexts.DONE, false, this::close));
    }

    /** The first click arms Reset and the second does it, because it can't be undone. */
    private void pressReset() {
        long now = Util.getMeasuringTimeMs();
        if (now < this.resetArmedUntil) {
            this.resetArmedUntil = 0L;
            this.commitTextEditor();
            this.resetAction.run();
            this.resetButton.setMessage(Text.translatable("screen.config_ui.reset"));
            this.flash(Text.translatable("screen.config_ui.reset.done"));
        } else {
            this.resetArmedUntil = now + RESET_CONFIRM_MS;
            this.resetButton.setMessage(Text.translatable("screen.config_ui.reset.confirm"));
        }
    }

    private void openCategory(int index) {
        if (index != this.category) {
            this.category = index;
            this.openPage();
        }
    }

    private void openSection(int index) {
        if (index != this.openSections[this.category]) {
            this.openSections[this.category] = index;
            this.openPage();
        }
    }

    /**
     * Rebuilds on the next frame. A click sets focus on its widget after the widget's action runs, so
     * rebuilding straight away would leave focus on a widget that's just been removed.
     */
    private void openPage() {
        this.query = "";
        this.resetArmedUntil = 0L;
        this.rebuildPending = true;
    }

    /** Shows a short message in the footer. */
    void flash(Text message) {
        this.flashMessage = message;
        this.flashUntil = Util.getMeasuringTimeMs() + FLASH_MS;
    }

    /** Opens a text box over a card, for String settings. */
    void editText(UiSetting<String> setting, int x, int y, int width) {
        this.commitTextEditor();
        TextFieldWidget editor = new TextFieldWidget(this.textRenderer, x, y, width, 16, setting.name());
        editor.setMaxLength(1024);
        editor.setText(setting.get());
        this.editedSetting = setting;
        this.textEditor = this.addDrawableChild(editor);
        // The click that got here focuses the grid once it finishes, so focus the box on the next frame.
        this.focusTextEditor = true;
    }

    private void commitTextEditor() {
        if (this.textEditor != null) {
            this.editedSetting.set(this.textEditor.getText());
            this.remove(this.textEditor);
            this.textEditor = null;
            this.editedSetting = null;
        }
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.search);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(context, mouseX, mouseY, partialTick);
        this.renderWindow(context);
    }

    private void renderWindow(DrawContext context) {
        int x = this.windowX;
        int y = this.windowY;
        int right = x + this.windowWidth;
        int bottom = y + this.windowHeight;

        context.fill(x - 1, y - 1, right + 1, bottom + 1, ConfigUiStyle.BLACK);
        context.fill(x, y, right, bottom, ConfigUiStyle.BASE);

        // The top bar. An amber rule under it is all the theming there is.
        context.fill(x, y, right, y + BAR_HEIGHT, ConfigUiStyle.RAIL);
        context.fill(x, y + BAR_HEIGHT, right, y + BAR_HEIGHT + 1, ConfigUiStyle.ACCENT);

        context.fill(x, y + BAR_HEIGHT + 1, x + this.railWidth, bottom, ConfigUiStyle.RAIL);
        context.fill(x + this.railWidth, this.footerY, right, this.footerY + 1, ConfigUiStyle.LINE);

        int titleX = x + TITLE_INSET;
        String title = this.title.getString();
        context.drawText(this.textRenderer, title, titleX, y + 6, ConfigUiStyle.TITLE, true);

        int titleWidth = this.textRenderer.getWidth(title);
        int pathSpace = this.windowWidth - TITLE_INSET - titleWidth - Math.min(130, this.windowWidth / 3) - 16;

        ConfigUiStyle.label(
                context, this.textRenderer,
                ConfigUiStyle.fit(this.textRenderer, this.pathText(), pathSpace),
                titleX + titleWidth + 6, y + 6, ConfigUiStyle.TEXT_FAINT
        );
    }

    private String pathText() {
        String path = "> " + this.currentCategory().name().getString();
        if (this.currentCategory().sections().size() > 1) {
            path += " > " + this.currentSection().name().getString();
        }
        return path;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        if (this.rebuildPending) {
            this.rebuildPending = false;
            this.clearAndInit();
        }

        if (this.focusTextEditor) {
            this.focusTextEditor = false;
            if (this.textEditor != null) {
                this.setFocused(this.textEditor);
            }
        }

        if (this.resetArmedUntil != 0L && Util.getMeasuringTimeMs() >= this.resetArmedUntil) {
            this.resetArmedUntil = 0L;
            this.resetButton.setMessage(Text.translatable("screen.config_ui.reset"));
        }

        super.render(context, mouseX, mouseY, partialTick);

        Text description = this.currentSection().description();
        if (description != null) {
            ConfigUiStyle.label(context, this.textRenderer, ConfigUiStyle.fit(this.textRenderer, description.getString(), this.contentWidth), this.contentX, this.descriptionY, ConfigUiStyle.TEXT_FAINT);
        }

        if (this.grid.itemCount() == 0) {
            int y = this.descriptionY + (description != null ? 15 : 2);
            ConfigUiStyle.label(context, this.textRenderer, Text.translatable("screen.config_ui.no_matches").getString(), this.contentX, y, ConfigUiStyle.TEXT_FAINT);
        }

        int statusWidth = this.windowX + this.windowWidth - 6 - BUTTON_WIDTH * 2 - 6 - 6 - this.contentX;
        ConfigUiStyle.label(
                context, this.textRenderer,
                ConfigUiStyle.fit(this.textRenderer, this.footerText(), statusWidth),
                this.contentX, this.footerY + 8, ConfigUiStyle.TEXT_FAINT
        );
    }

    /** The footer shows the most specific thing available: a message, the hovered card, then the page. */
    private String footerText() {
        if (this.flashMessage != null && Util.getMeasuringTimeMs() < this.flashUntil) {
            return this.flashMessage.getString();
        }

        Text hint = this.grid.hoverHint();
        if (hint != null) {
            return hint.getString();
        }

        String text = this.status.get();
        Text note = this.currentSection().note().get();
        if (note != null) {
            text = text.isEmpty() ? note.getString() : text + ". " + note.getString();
        }

        return text;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.textEditor != null && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE)) {
            this.commitTextEditor();
            this.setFocused(this.grid);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Clicking a card moves focus off the search box, so keep typing working without clicking back.
        // Space is left alone, because it also presses whatever has keyboard focus.
        Element focused = this.getFocused();
        boolean typingInBox = focused == this.search || (this.textEditor != null && focused == this.textEditor);

        if (!typingInBox && !Character.isWhitespace(codePoint)) {
            this.setFocused(this.search);
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.textEditor != null && !this.textEditor.isMouseOver(mouseX, mouseY)) {
            this.commitTextEditor();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.commitTextEditor();
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void close() {
        this.commitTextEditor();
        this.spec.save().run();
        this.client.setScreen(this.parent);
    }
}