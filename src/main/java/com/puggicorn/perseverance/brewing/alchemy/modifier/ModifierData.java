package com.puggicorn.perseverance.brewing.alchemy.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public record ModifierData(
        Ingredient modifier,
        Item validInputItem,
        Item targetItem
) {

    public static final Codec<ModifierData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("modifier").forGetter(ModifierData::modifier),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("valid_input_item").forGetter(ModifierData::validInputItem),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("target_item").forGetter(ModifierData::targetItem)
    ).apply(instance, ModifierData::new));

    public boolean isValidInput(Item currentPotionItem) {
        return this.validInputItem == currentPotionItem;
    }
}