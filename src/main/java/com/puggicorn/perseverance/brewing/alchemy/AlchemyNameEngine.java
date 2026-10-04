package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public class AlchemyNameEngine {

    // TODO: Add language key support so people can create in-game translations
    public static Component getDynamicName(ItemStack stack) {
        var baseComponent = stack.get(ModDataComponents.BASE_POTION_TYPE.get());
        if (baseComponent == null) {
            return Component.translatable("item.perseverance_brewing.potion.fallback");
        }

        String baseID = baseComponent.baseID();
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        // Placeholder, it will later be more dynamic
        if (contents != null && contents.hasEffects()) {
            return Component.translatable("item.perseverance_brewing.potion.custom");
        }

        String baseKey = "item.perseverance_brewing.base_potion." + baseID.toLowerCase();
        return Component.translatable(baseKey);
    }

}
