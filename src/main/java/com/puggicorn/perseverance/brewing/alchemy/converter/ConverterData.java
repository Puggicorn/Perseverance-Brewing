package com.puggicorn.perseverance.brewing.alchemy.converter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;

public record ConverterData(Ingredient converter, Map<ResourceLocation, ConverterData.ConversionTarget> conversions) {

    public record ConversionTarget(Holder<MobEffect> target, double durationMultiplier, double amplifierMultiplier) {

        public static final Codec<ConversionTarget> DETAILED_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("target").forGetter(ConversionTarget::target),
                Codec.DOUBLE.optionalFieldOf("duration", 1.0).forGetter(ConversionTarget::durationMultiplier),
                Codec.DOUBLE.optionalFieldOf("amplifier", 1.0).forGetter(ConversionTarget::amplifierMultiplier)
        ).apply(inst, ConversionTarget::new));

        public static final Codec<ConversionTarget> CODEC = Codec.either(
                BuiltInRegistries.MOB_EFFECT.holderByNameCodec(),
                DETAILED_CODEC
        ).xmap(
                either -> either.map(
                        holder -> new ConversionTarget(holder, 1.0, 1.0),
                        detailed -> detailed
                ),
                target -> Either.right(target)
        );
    }

    public static final Codec<ConverterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("converter").forGetter(ConverterData::converter),
            Codec.unboundedMap(
                    ResourceLocation.CODEC,
                    ConversionTarget.CODEC
            ).fieldOf("conversions").forGetter(ConverterData::conversions)
    ).apply(instance, ConverterData::new));

    public boolean canConvert(Holder<MobEffect> originalEffect) {
        ResourceLocation effectKey = BuiltInRegistries.MOB_EFFECT.getKey(originalEffect.value());
        return effectKey != null && conversions.containsKey(effectKey);
    }

    public ConversionTarget getConversion(Holder<MobEffect> originalEffect) {
        ResourceLocation effectKey = BuiltInRegistries.MOB_EFFECT.getKey(originalEffect.value());
        if (effectKey == null) return null;
        return conversions.get(effectKey);
    }
}
