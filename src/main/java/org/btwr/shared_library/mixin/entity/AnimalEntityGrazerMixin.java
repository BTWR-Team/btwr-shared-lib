package org.btwr.shared_library.mixin.entity;

import net.minecraft.entity.passive.AnimalEntity;
import org.btwr.shared_library.api.entity.interfaces.Grazer;
import org.spongepowered.asm.mixin.Mixin;

/** Only adds the {@link Grazer} interface with its defaults. Mods override its methods per animal. **/
@Mixin(AnimalEntity.class)
public abstract class AnimalEntityGrazerMixin implements Grazer {}