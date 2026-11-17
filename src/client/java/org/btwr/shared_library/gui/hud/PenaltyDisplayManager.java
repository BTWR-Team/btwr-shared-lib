package org.btwr.shared_library.gui.hud;

import org.btwr.shared_library.BTWRSLMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.btwr.shared_library.config.BTWRSLModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Handles rendering of penalties, and their hierarchy
 */
public class PenaltyDisplayManager {
    /**
     * Instance of PenaltyDisplayManager
     */
    private static final PenaltyDisplayManager INSTANCE = new PenaltyDisplayManager();

    public static final int HEALTH_PRIORITY = 10;
    public static final int GLOOM_PRIORITY = 20;
    public static final int HUNGER_PRIORITY = 30;

    public static final byte RIGHT = 0x1;
    public static final byte LEFT = 0x0;
    public static final byte BOTTOM = 0x1;
    public static final byte TOP = 0x0;

    /**
     * List of current penalties to be rendered
     */
    private static TreeMap<Integer,Penalty> penalties = new TreeMap<>();

    public static PenaltyDisplayManager getInstance() {
        return INSTANCE;
    }

    /**
     * Lambda condition test that determines rendering
     */
    @FunctionalInterface
    public interface PenaltyCondition {
        boolean test();
    }

    /**
     * Lambda string getter that determines text drawn
     */
    @FunctionalInterface
    public interface PenaltyString {
        String get();
    }

    /**
     * Stores information on how and when to draw a penalty
     */
    static public class Penalty {
        /**
         * Determines rendering order, with lower values being closer to the hotbar
         */
        protected int priority;
        /**
         * String that will be rendered
         */
        protected PenaltyString display;
        /**
         * Condition that must pass in order for text to render
         */
        protected PenaltyCondition condition;

        /**
         * Creates a penalty with variable text and conditions
         * @param priority Order this penalty will be drawn
         * @param display Variable text to draw
         * @param condition Variable condition that must pass for this penalty to draw
         */
        public Penalty(int priority, PenaltyString display, PenaltyCondition condition) {
            this.priority = priority;
            this.display = display;
            this.condition = condition;
        }

        /**
         * Creates a penalty with a static string and variable conditions
         * @param priority Order this penalty will be drawn
         * @param string Text to draw
         * @param condition Variable condition that must pass for this penalty to draw
         */
        public Penalty(int priority, String string, PenaltyCondition condition) {
            this.priority = priority;
            this.display = () -> string;
            this.condition = condition;
        }

        /**
         * Creates a penalty with a variable string that will always draw
         * @param priority Order this penalty will be drawn
         * @param display Variable text to draw
         */
        public Penalty(int priority, PenaltyString display) {
            this.priority = priority;
            this.display = display;
            this.condition = () -> true;
        }

        /**
         * Gets the priority level for this penalty
         */
        public int getPriority() {
            return this.priority;
        }

        /**
         * Gets current displayed text for this penalty
         */
        public String getDisplay() {
            return this.display.get();
        }
    }

    public void render(DrawContext context, TextRenderer renderer) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        this.renderPenalties(context, renderer, player);
    }

    /**
     * Draws penalties (if applicable) onto provided context
     * @param context DrawContext to draw to
     * @param renderer TextRenderer to use for drawing text
     * @param player PlayerEntity to get information from
     */
    private void renderPenalties(DrawContext context, TextRenderer renderer, PlayerEntity player) {
        // Determine if draw should be diverted
        if (getDrawMode().isAnchor()) {
            renderPenaltiesAnchored(context, renderer, player);
            return;
        }

        // Calculate the position of the hunger bar
        int hungerBarX = context.getScaledWindowWidth() / 2 + 91;  // Center of the hunger bar
        int hungerBarY = context.getScaledWindowHeight() - 39; // Hunger bar position vertically

        // Add override if applicable
        hungerBarX += BTWRSLModConfig.renderXOffset.get();
        hungerBarY -= BTWRSLModConfig.renderYOffset.get();

        // Get y position to start drawing penalties
        int textY = this.getTextY(player, hungerBarY);
        int offsetY = 0;

        // Begin drawing
        for (Penalty penalty : penalties.values()) {
            // Get translated text
            Text translatedText = Text.translatable(penalty.getDisplay());

            // Right align text to hot-bar
            int statusX = hungerBarX - renderer.getWidth(translatedText);

            // Render
            if (penalty.condition.test() && !translatedText.getString().isEmpty()) {
                context.drawText(renderer, translatedText, statusX, textY - offsetY, 0xFFFFFFFF, true);
                offsetY += 10;
            }
        }

        // Set render boolean to false in-case of HUD changes at runtime
        StatusBarRenderInfo.getInstance().setRenderingFood(false);
    }

    private void renderPenaltiesAnchored(DrawContext context, TextRenderer renderer, PlayerEntity player) {
        // Calculate anchor point
        PenaltyDrawMode drawMode = getDrawMode();
        int renderX = drawMode.getX(context);
        int renderY = drawMode.getY(context);
        int offsetY = 0;
        int margin = BTWRSLModConfig.renderMargin.get();

        // Add offsets if applicable
        renderX += BTWRSLModConfig.renderXOffset.get();
        renderX += drawMode.getXMargin(margin);
        renderY -= BTWRSLModConfig.renderYOffset.get();
        renderY += drawMode.getYMargin(margin);

        // Begin drawing
        for (Penalty penalty : penalties.values()) {
            // Get translated text
            Text translatedText = Text.translatable(penalty.getDisplay());

            // Right align text to hot-bar
            int statusX = renderX - (drawMode.shouldAddX() ? renderer.getWidth(translatedText) : 0);

            // Render
            if (penalty.condition.test() && !translatedText.getString().isEmpty()) {
                context.drawText(renderer, translatedText, statusX, renderY - offsetY, 0xFFFFFFFF, true);
                offsetY += drawMode.shouldFlipY() ? -10 : 10;
            }
        }
    }

    /**
     * Calculates proper Y position for HUD elements
     * @param player PlayerEntity to get information from
     * @param hungerBarY Y position of the hunger bar
     * @return Calculated Y position
     */
    private int getTextY(PlayerEntity player, int hungerBarY) {
        // Get additional context
        HudRenderInfo info = HudRenderInfo.createDefault(hungerBarY);

        // Default Y position (above the hunger bar, alternatively above hot-bar)
        int baseY = StatusBarRenderInfo.getInstance().getRenderingFood() ? hungerBarY - 10 : hungerBarY;

        return baseY + HudYOffsetRegistry.computeTotalYOffset(player, info);
    }

    /**
     * Adds penalty to TreeMap
     * @param newPenalty Penalty to add
     */
    public void addPenalty(Penalty newPenalty) {
        // Get index and verify another key doesn't exist
        int index = newPenalty.getPriority();
        boolean hasKey = penalties.containsKey(index);

        // If a key exists, continue bumping index until we reach an open key
        while (hasKey) {
            hasKey = penalties.containsKey(++index);
        }
        penalties.put(index, newPenalty);
    }

    /**
     * Removes penalty from TreeMap
     * @param newPenalty Penalty to remove
     */
    public void removePenalty(Penalty newPenalty) {
        int index = newPenalty.getPriority();
        removePenaltyAtIndex(index);
    }

    /**
     * Removes penalty at index from TreeMap
     * @param index Index to attempt removal at
     */
    public void removePenaltyAtIndex(int index) {
        try {
            penalties.remove(index);
        } catch (IndexOutOfBoundsException e) {
            BTWRSLMod.LOGGER.error("Could not remove penalty!", e.getCause());
        }
    }

    /**
     * Readability wrapper for draw mode
     * @return Current draw mode
     */
    private PenaltyDrawMode getDrawMode() {
        return BTWRSLModConfig.drawMode.get();
    }

    /**
     * Register dynamic changes to the default Y position of Penalty texts displayed by {@link PenaltyDisplayManager}
     */
    public static final class HudYOffsetRegistry {
        private static final List<HudYOffsetRule> RULES = new ArrayList<>();

        public static void register(HudYOffsetRule rule) {
            RULES.add(rule);
        }

        // Default rules for offsetting the text
        public static void registerDefaults() {
            // Player is underwater and food is rendered
            register((player, info) ->
                    StatusBarRenderInfo.getInstance().getRenderingAir(player) && StatusBarRenderInfo.getInstance().getRenderingFood() ? -10 : 0
            );
            // Player is wearing armor and food is NOT rendered
            register((player, info) ->
                    StatusBarRenderInfo.getInstance().getRenderingArmor(player) && !StatusBarRenderInfo.getInstance().getRenderingFood() ? -10 : 0
            );
        }

        public static int computeTotalYOffset(PlayerEntity player, HudRenderInfo info) {
            int offset = 0;
            for (HudYOffsetRule rule : RULES) {
                offset += rule.getYOffset(player, info);
            }
            return offset;
        }

        @FunctionalInterface
        public interface HudYOffsetRule {
            /**
             * @return The vertical offset this rule wants to apply (can be 0).
             */
            int getYOffset(PlayerEntity player, HudRenderInfo info);
        }

    }

    /** Context record for keeping track of what the current hunger bar Y is and whether {@link PenaltyDisplayManager} is rendering food **/
    public record HudRenderInfo(int hungerBarY, boolean renderingFood) {
        public static HudRenderInfo createDefault(int hungerBarY) {
            return new HudRenderInfo(hungerBarY, StatusBarRenderInfo.getInstance().getRenderingFood());
        }
    }
}