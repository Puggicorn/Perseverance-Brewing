package com.puggicorn.perseverance.brewing.mixin.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(PotionContents.class)
public class PotionContentsMixin {

    // Dirty patch for the tooltip assuming the lingering potion cloud will reduce the effects, even when it doesn't.
    @Inject(
            method = "addPotionTooltip(Ljava/util/function/Consumer;FF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bypassLingeringVisualScale(Consumer<Component> tooltip, float durationFactor, float tickRate, CallbackInfo ci) {
        PotionContents self = (PotionContents) (Object) this;

        // Checks to see if the client is trying to bypass.
        if (durationFactor == 0.25F || durationFactor == 0.125F) {

            boolean isCustomBrew = !self.customEffects().isEmpty();

            if (isCustomBrew) {
                self.addPotionTooltip(tooltip, 1.0F, tickRate);

                ci.cancel();
            }
        }
    }
}
