package com.puggicorn.perseverance.brewing.effect;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, PerseveranceBrewingMod.MODID);

    public static final DeferredHolder<MobEffect, ClimbingEffect> CLIMBING = MOB_EFFECTS.register("climbing", ClimbingEffect::new);
    public static final DeferredHolder<MobEffect, BurningRageEffect> BURNING_RAGE = MOB_EFFECTS.register("burning_rage", () -> new BurningRageEffect().withModifiers());
    public static final DeferredHolder<MobEffect, StunEffect> STUN = MOB_EFFECTS.register("stun", StunEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
