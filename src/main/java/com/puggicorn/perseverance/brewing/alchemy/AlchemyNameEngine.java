package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class AlchemyNameEngine {

    // Dynamic Names
    public static String computeDynamicName(ItemStack stack) {
        // Potion Type / Modifier
        String containerPrefix = "Potion";
        if (stack.is(Items.SPLASH_POTION)) containerPrefix = "Splash Potion";
        else if (stack.is(Items.LINGERING_POTION)) containerPrefix = "Lingering Potion";

        String additivePrefix = "";
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        // TODO: Implement when Additives are added

        // Potion Effect Name Combos
        if (true) {


            // Generic Fallback
            return additivePrefix + containerPrefix + " of Mixed Alchemy";
        }

        // Base Potion Name
        String baseName = "Unknown";
        if (stack.has(ModDataComponents.BASE_POTION_TYPE.get())) {
            var baseComponent = stack.get(ModDataComponents.BASE_POTION_TYPE.get());
            if (baseComponent != null) {
                // Maybe replace this later with a language key reader for non-English speakers?
                baseName = baseComponent.baseName();
            }
        }

        // Base fallback
        return additivePrefix + " of " + baseName;
    }

}
