package com.puggicorn.perseverance.brewing.mixin.vanilla;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(InstantenousMobEffect.class)
public abstract class InstantenousMobEffectMixin extends MobEffect {

    private InstantenousMobEffectMixin(net.minecraft.world.effect.MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isInstantenous() {
        InstantenousMobEffect effect = (InstantenousMobEffect) (Object) this;

        // Forces Saturation to run its frames, like it would as a stew.
        if (effect == MobEffects.SATURATION) {
            return false;
        }

        return super.isInstantenous();
    }
}
