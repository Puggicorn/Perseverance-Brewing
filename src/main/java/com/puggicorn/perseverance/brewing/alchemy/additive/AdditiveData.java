package com.puggicorn.perseverance.brewing.alchemy.additive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;

public record AdditiveData(
        Ingredient additive,
        String additiveID,
        double durationMultiplier,
        int durationFlatBonus,
        int amplifierIncrease,
        int maxAmplifierLimit
) {

    public static final Codec<AdditiveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("additive").forGetter(AdditiveData::additive),
            Codec.STRING.fieldOf("additive_id").forGetter(AdditiveData::additiveID),
            Codec.DOUBLE.optionalFieldOf("duration_multiplier", 1.0).forGetter(AdditiveData::durationMultiplier),
            Codec.INT.optionalFieldOf("duration_flat_bonus", 0).forGetter(AdditiveData::durationFlatBonus),
            Codec.INT.optionalFieldOf("amplifier_increase", 0).forGetter(AdditiveData::amplifierIncrease),
            Codec.INT.optionalFieldOf("max_amplifier_limit", 1).forGetter(AdditiveData::maxAmplifierLimit)
    ).apply(instance, AdditiveData::new));
}