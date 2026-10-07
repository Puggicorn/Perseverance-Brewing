package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlchemyNameEngine {

    public static Component getDynamicName(ItemStack stack) {
        var baseComponent = stack.get(ModDataComponents.BASE_POTION_TYPE.get());
        if (baseComponent == null) {
            return Component.translatable("item.perseverance_brewing.potion.fallback");
        }


        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        // [Step 1] Use the name of the base if no effects are present
        if (contents == null || !contents.hasEffects()) {
            String baseID = baseComponent.baseID();
            return Component.translatable("item.perseverance_brewing.base_potion." + baseID.toLowerCase());
        }

        List<MobEffectInstance> effects = contents.customEffects();

        // [Step 2] Create an alphanumeric signature for unique potion lang keys. This is only used in special cases.
        String recipeSignature = createRecipeSignature(effects);
        String specificLangKey = "item.perseverance_brewing.potion.recipe." + recipeSignature;

        Component.translatable(specificLangKey).getString();
        if (!Component.translatable(specificLangKey).getString().equals(specificLangKey)) {
            return Component.translatable(specificLangKey);
        }

        // [Step 3] If a unique potion key doesn't have an entry, generate a name based on the effects present.
        int effectCount = Math.min(effects.size(), 2);

        // Extract word blocks using the dynamic noun/adjective state system
        Component primaryWord = getEffectForm(effects.get(0), false); // index 0 is always the Lead Noun
        Component secondaryWord = effectCount > 1 ? getEffectForm(effects.get(1), true) : Component.empty(); // index 1 is an Adjective

        // Set the additive name if applicable
        var additive = stack.get(ModDataComponents.ADDITIVE_COMPONENT.get());
        boolean hasAdditive = additive != null;

        // Routing Logic Layer matching each of the language keys:
        if (effectCount == 1) {
            Component vanillaName = Component.translatable(effects.getFirst().getDescriptionId());
            if (!hasAdditive) {
                // 1 Effect, No Additive -> "Potion of %1$s"
                return Component.translatable("item.perseverance_brewing.potion.custom.container1", vanillaName);
            } else {
                // 1 Effect + Additive -> "%2$s Potion of %1$s"
                Component additivePrefix = Component.translatable("alchemy.additive." + additive.additiveId());
                return Component.translatable("item.perseverance_brewing.potion.custom.container3", vanillaName, additivePrefix);
            }
        } else {
            if (!hasAdditive) {
                // 2 Effects, No Additive -> "Potion of %2$s %1$s"
                return Component.translatable("item.perseverance_brewing.potion.custom.container2", primaryWord, secondaryWord);
            } else {
                // 2 Effects + Additive -> "%3$s Potion of %2$s %1$s"
                Component additivePrefix = Component.translatable("alchemy.additive." + additive.additiveId());
                return Component.translatable("item.perseverance_brewing.potion.custom.container4", primaryWord, secondaryWord, additivePrefix);
            }
        }
    }

    // Helper function to generate potion names with dynamic or vanilla fallback routing
    private static Component getEffectForm(MobEffectInstance instance, boolean preferAdjective) {
        String effectID = instance.getDescriptionId();
        String customKey = effectID + (preferAdjective ? ".adj" : ".noun");
        return Component.translatableWithFallback(customKey, Component.translatable(effectID).getString());
    }

    // Allows setting a specific lang key signature for potion effect combinations, with order independence. REQUIRES ALPHABETICAL ORDERING OF IDS
    // Intended use is for *specific* names for specific brews, such as the vanilla Turtle Master potion.
    // Example: "item.perseverance_brewing.potion.recipe.resistance_slowness": "Potion of the Turtle Master" -> Potion of the Turtle Master
    private static String createRecipeSignature(List<MobEffectInstance> effects) {
        List<String> sortedNames = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            var effectKey = BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value());
            if (effectKey != null) {
                sortedNames.add(effectKey.getPath());
            }
        }
        Collections.sort(sortedNames);
        return String.join("_", sortedNames);
    }

}
