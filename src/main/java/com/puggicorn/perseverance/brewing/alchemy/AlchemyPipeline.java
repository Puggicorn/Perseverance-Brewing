package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.alchemy.catalyst.BasePotionComponent;
import com.puggicorn.perseverance.brewing.alchemy.catalyst.CatalystLoader;
import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class AlchemyPipeline {

    // Step 1: Water Bottle + Catalyst -> Potion Base

    private static boolean isCatalyst(ItemStack ingredient) {
        return CatalystLoader.getCatalyst(ingredient).isPresent();
    }

    private static boolean canApplyCatalyst(ItemStack potion, ItemStack catalyst) {
        if (!potion.is(Items.POTION)) return false;

        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.is(Potions.WATER)) return false;

        return !potion.has(ModDataComponents.BASE_POTION_TYPE.get());
    }

    private static ItemStack applyCatalyst(ItemStack potion, ItemStack catalyst) {
        ItemStack result = potion.copyWithCount(1);
        result.remove(DataComponents.POTION_CONTENTS);

        CatalystLoader.getCatalyst(catalyst).ifPresent(data -> {
            result.set(ModDataComponents.BASE_POTION_TYPE.get(),
                    new BasePotionComponent(data.baseID(), data.baseName(), data.color()));
        });

        return result;
    }


    // Step 2: Potion Base + Reagent -> Potion with Effects

    private static boolean isReagent(Level level, ItemStack ingredient) {
        // TODO: Read custom registry/datapack recipes
        return false;
    }
    private static boolean canApplyReagent(Level level, ItemStack potion, ItemStack reagent) {
        // Can only overlay data effects if the target potion container has a valid Base rule profile
        return true;
    }
    private static ItemStack applyReagent(Level level, ItemStack potion, ItemStack reagent) {
        // TODO: Parse the ingredient's JSON effects map and overlay them onto the stack
        return potion;
    }


    // Step 3: Potion with Effects + Converter -> Potion with new Effects [Optional]

    private static boolean isConverter(Level level, ItemStack ingredient) {
        // TODO: Read data-driven conversion definitions
        return false;
    }
    private static boolean canApplyConverter(Level level, ItemStack potion, ItemStack converter) {
        // Check if the current potion possesses an effect mapped inside the converter's conversion dictionary
        return true;
    }
    private static ItemStack applyConverter(Level level, ItemStack potion, ItemStack converter) {
        // TODO: Invert or exchange old effect states for configured outputs
        return potion;
    }


    // Step 4: Apply an additive -> potion effects change stats [Optional]

    private static boolean isAdditive(ItemStack ingredient) {
        // TODO: Identify statutory items
        return false;
    }
    private static boolean canApplyAdditive(ItemStack potion, ItemStack additive) {
        return true;
    }
    private static ItemStack applyAdditive(ItemStack potion, ItemStack additive) {
        // TODO: Increment Duration parameters or scale Amplifier tier limits
        return potion;
    }


    // Step 5: Apply modifiers -> Potion to Splash, Lingering, Etc

    private static boolean isModifier(ItemStack ingredient) {
        // TODO: Identify functional delivery items (Gunpowder, Dragon Breath)
        return false;
    }
    private static boolean canApplyModifier(ItemStack potion, ItemStack modifier) {
        return true;
    }
    private static ItemStack applyModifier(ItemStack potion, ItemStack modifier) {
        // TODO: Shift Item registry pointers (Normal -> Splash -> Lingering)
        return potion;
    }


    // Brewing checks

    public static  boolean isValidPipelineIngredient(Level level, ItemStack ingredient) {
        if (ingredient.isEmpty()) return false;

        return     isCatalyst(ingredient)
                || isReagent(level, ingredient)
                || isConverter(level, ingredient)
                || isAdditive(ingredient)
                || isModifier(ingredient);
    }

    public static boolean canProcess(Level level, PotionBrewing brewing, NonNullList<ItemStack> items) {
        ItemStack ingredient = items.get(3);
        if (!isValidPipelineIngredient(level, ingredient)) return false;

        for (int i = 0; i < 3; i++) {
            ItemStack potionStack = items.get(i);
            if (potionStack.isEmpty()) continue;

            if (isCatalyst(ingredient) && canApplyCatalyst(potionStack, ingredient)) return true;
            if (isReagent(level, ingredient) && canApplyReagent(level, potionStack, ingredient)) return true;
            if (isConverter(level, ingredient) && canApplyConverter(level, potionStack, ingredient)) return true;
            if (isAdditive(ingredient) && canApplyAdditive(potionStack, ingredient)) return  true;
            if (isModifier(ingredient) && canApplyModifier(potionStack, ingredient)) return true;
        }
        return false;
    }


    // Execution
    public static void executeBrewCycle(Level level, NonNullList<ItemStack> items) {
        ItemStack ingredient = items.get(3);

        for (int i = 0; i < 3; i++) {
            ItemStack potionStack = items.get(i);
            if (potionStack.isEmpty()) continue;;

            if (isCatalyst(ingredient) && canApplyCatalyst(potionStack, ingredient)) {
                items.set(i, applyCatalyst(potionStack, ingredient));
            }
            else if (isReagent(level, ingredient) && canApplyReagent(level, potionStack, ingredient)) {
                items.set(i, applyReagent(level, potionStack, ingredient));
            }
            else if (isConverter(level, ingredient) && canApplyConverter(level, potionStack, ingredient)) {
                items.set(i, applyConverter(level, potionStack, ingredient));
            }
            else if (isAdditive(ingredient) && canApplyAdditive(potionStack, ingredient)) {
                items.set(i, applyAdditive(potionStack, ingredient));
            }
            else if (isModifier(ingredient) && canApplyModifier(potionStack, ingredient)) {
                items.set(i, applyModifier(potionStack, ingredient));
            }
        }
    }
}
