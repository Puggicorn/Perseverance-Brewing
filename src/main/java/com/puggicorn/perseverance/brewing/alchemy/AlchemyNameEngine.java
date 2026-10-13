package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlchemyNameEngine {

    public static Component getDynamicName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        var baseComponent = stack.get(ModDataComponents.BASE_POTION_TYPE.get());
        if (baseComponent == null || contents == null) {
            Component fallbackName = Component.translatable("item.perseverance_brewing.potion.fallback");

            return finalizeName(fallbackName);
        }


        // [Step 1] Use the name of the base if no effects are present

        List<MobEffectInstance> effects = new ArrayList<>();
        if (contents.potion().isPresent()) {
            effects.addAll(contents.potion().get().value().getEffects());
        }
        effects.addAll(contents.customEffects());

        if (effects.isEmpty()) {
            String baseID = baseComponent.baseID();
            Component baseName = Component.translatable("item.perseverance_brewing.base_potion." + baseID.toLowerCase());

            return finalizeName(baseName);
        }

        // [Step 2] Create an alphanumeric signature for unique potion lang keys. This is only used in special cases.
        String recipeSignature = createRecipeSignature(effects);
        String specificLangKey = "item.perseverance_brewing.potion.recipe." + recipeSignature;

        Component fallbackDynamicName = buildMasterTemplateName(stack, effects);

        // [Step 3] If a unique potion key doesn't have an entry, generate a name based on the effects present.
        Component generatedName = Component.translatableWithFallback(specificLangKey, fallbackDynamicName.getString());

        return finalizeName(generatedName);
    }

    private static Component finalizeName(Component component) {
        return component.copy().withStyle(Style.EMPTY.withItalic(false));
    }

    private static Component buildMasterTemplateName(ItemStack stack, List<MobEffectInstance> effects) {
        int effectCount = Math.min(effects.size(), 2);

        Component primaryNoun = effectCount == 1
                ? Component.translatable(effects.getFirst().getDescriptionId())
                : getEffectForm(effects.get(0), false);

        Component secondaryAdj = effectCount > 1
                ? getEffectForm(effects.get(1), true)
                : Component.translatable("alchemy.effect.none");

        var additive = stack.get(ModDataComponents.ADDITIVE_COMPONENT.get());
        Component additivePrefix = additive != null
                ? Component.translatable("alchemy.additive." + additive.additiveID())
                : Component.translatable("alchemy.additive.none");

        var modifier = stack.get(ModDataComponents.MODIFIER_COMPONENT.get());
        Component modPrefix = modifier != null
                ? Component.translatable("alchemy.modifier." + modifier.modifierID())
                : Component.translatable("alchemy.modifier.none");

        return Component.translatable(
                "item.perseverance_brewing.potion.master_template",
                primaryNoun, secondaryAdj, additivePrefix, modPrefix
        );
    }

    // Helper function to generate potion names with dynamic or vanilla fallback routing
    private static Component getEffectForm(MobEffectInstance instance, boolean preferAdjective) {
        String effectID = instance.getDescriptionId();
        String customKey = effectID + (preferAdjective ? ".adj" : ".noun");

        return Component.translatableWithFallback(customKey, "%s".formatted(Component.translatable(effectID)));
    }

    // Allows setting a specific lang key signature for potion effect combinations, with order independence. REQUIRES ALPHABETICAL ORDERING OF IDS
    // Intended use is for *specific* names for specific brews, such as the vanilla Turtle Master potion.
    // Example: "item.perseverance_brewing.potion.recipe.resistance_slowness": "Potion of the Turtle Master" -> Potion of the Turtle Master
    private static String createRecipeSignature(List<MobEffectInstance> effects) {
        List<String> sortedNames = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            var effectKey = BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value());
            if (effectKey != null) {
                String identifier = effectKey.getNamespace() + "." + effectKey.getPath();
                sortedNames.add(identifier);
            }
        }
        Collections.sort(sortedNames);
        return String.join("_", sortedNames);
    }

}
