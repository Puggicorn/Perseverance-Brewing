package com.puggicorn.perseverance.brewing.alchemy.reagent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record ReagentData(Ingredient reagent, List<ReagentEffectInstance> effects) {
    public static final Codec<ReagentData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("reagent").forGetter(ReagentData::reagent),
                    ReagentEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(ReagentData::effects)
            ).apply(instance, ReagentData::new)
    );
}
