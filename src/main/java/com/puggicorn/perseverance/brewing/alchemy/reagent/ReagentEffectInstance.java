package com.puggicorn.perseverance.brewing.alchemy.reagent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public record ReagentEffectInstance(Holder<MobEffect> effect, int duration, int amplifier) {
    public static final Codec<ReagentEffectInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("id").forGetter(ReagentEffectInstance::effect),
                    Codec.INT.optionalFieldOf("duration", 3600).forGetter(ReagentEffectInstance::duration),
                    Codec.INT.optionalFieldOf("amplifier", 0).forGetter(ReagentEffectInstance::amplifier)
            ).apply(instance, ReagentEffectInstance::new)
    );
}
