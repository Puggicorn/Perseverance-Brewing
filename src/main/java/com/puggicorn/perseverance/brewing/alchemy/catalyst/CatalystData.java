package com.puggicorn.perseverance.brewing.alchemy.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;

public record CatalystData(Ingredient catalyst, String baseStrategy, String baseID, int color) {

    private static final Codec<Integer> HEX_COLOR_CODEC = Codec.STRING.comapFlatMap(
            hex -> DataResult.success(Integer.parseInt(hex.trim().replace("#", ""), 16)),
            integer -> "#" + String.format("%06X", integer)
    );

    public static final Codec<CatalystData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("catalyst").forGetter(CatalystData::catalyst),
                    Codec.STRING.fieldOf("base_strategy").forGetter(CatalystData::baseStrategy),
                    Codec.STRING.fieldOf("base_id").forGetter(CatalystData::baseID),
                    HEX_COLOR_CODEC.fieldOf("color").forGetter(CatalystData::color)
            ).apply(instance, CatalystData::new)
    );
}
