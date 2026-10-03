package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public class AlchemyNameEngine {

    // TODO: Add language key support so people can create in-game translations
    public static String getDynamicName(ItemStack stack) {
        var baseComponent = stack.get(ModDataComponents.BASE_POTION_TYPE.get());
        if (baseComponent == null) return "Potion";

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        // Placeholder, it will later be more dynamic
        if (contents != null && contents.hasEffects()) {
            return "Custom Potion";
        }

        return baseComponent.baseName() + " Potion Base";
    }

}
