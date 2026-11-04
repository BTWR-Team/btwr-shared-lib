package org.btwr.shared_library.api.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BTWREvents {

    /**
     * Fired once per tick for every {@link LivingEntity}.
     * <p>
     * Hooks at the end of the entity's {@code tick} method, so all vanilla tick logic for that
     * entity has already run by the time handlers are called.
     * <p>
     * Handlers receive the entity being ticked. This event carries no result, so handlers
     * cannot cancel or alter the tick; they can only observe or modify the entity directly.
     * <p>
     * This runs for every living entity every tick, so keep handlers cheap and exit early
     * when the entity is not relevant.
     */
    public static final SimpleEvent<Consumer<LivingEntity>> LIVING_TICK = new SimpleEvent<>(
            handlers -> living -> handlers.forEach(c -> c.accept(living))
    );

    /**
     * Fired once per tick for every server-side, living {@link net.minecraft.entity.passive.AnimalEntity}
     * while it checks whether it should be startled.
     * <p>
     * Hooks at the end of the animal's {@code tickMovement} method. It is skipped on the client
     * and for dead animals.
     * <p>
     * Handlers receive an {@link AnimalStartleEvent} and use it to report what they found:
     * <ul>
     *   <li>{@link AnimalStartleEvent#startle(LivingEntity)} reports who startled the animal.
     *       The first handler to report an initiator wins.</li>
     *   <li>{@link AnimalStartleEvent#requestJump()} asks the animal to jump, for example to
     *       escape a suffocating block.</li>
     * </ul>
     * Once all handlers have run, the code that fired the event reads the results and applies
     * the actual reaction, so handlers should only report and not perform the reaction themselves.
     * <p>
     * Handlers run in registration order.
     */
    public static final SimpleEvent<Consumer<AnimalStartleEvent>> ANIMAL_STARTLE_TICK = new SimpleEvent<>(
            handlers -> event -> handlers.forEach(c -> c.accept(event))
    );

    /**
     * Fired on the server whenever an {@link AnimalEntity} eats an item, whether it was hand fed
     * or picked up off the ground. Breeding items that make the animal fall in love count too.
     * <p>
     * Vanilla hand feeding fires it from {@code AnimalEntity#eat}. Mods with their own eating
     * logic (like Animageddon's hunger) fire it themselves when the animal eats.
     * <p>
     * Handlers receive the animal and the eaten stack. It's fired before the stack is decremented,
     * so handlers can still check what was eaten, but should only read the stack and not change it.
     * <p>
     * Handlers run in registration order.
     */
    public static final SimpleEvent<BiConsumer<AnimalEntity, ItemStack>> ANIMAL_EAT_ITEM = new SimpleEvent<>(
            handlers -> (animal, stack) -> handlers.forEach(c -> c.accept(animal, stack))
    );

}