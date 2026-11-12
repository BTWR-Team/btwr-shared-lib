package org.btwr.shared_library.api.entity.interfaces;

/** Grazing related queries for animals, so block graze rules can check an animal without depending on the mod that implements its hunger.
 * <p>Injected into AnimalEntity. Everything defaults to false; mods override these per animal.
 * <p>Also check out {@link org.btwr.shared_library.api.registry.GrazeBehaviorRegistry}
 */
public interface Grazer {
    /** Starving animals can graze sparse vegetation **/
    default boolean btwr$isStarving() { return false; }

    /** Animals like pigs that dig up the earth they graze on  **/
    default boolean btwr$disruptsEarthOnGraze() { return false; }

    /** Animals like pigs that also eat flowers, dead bushes, pumpkins, etc. **/
    default boolean btwr$canGrazeOnRoughVegetation() { return false; }

    /** Animals like cows that can graze mycelium **/
    default boolean btwr$canGrazeMycelium() { return false; }
}