package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import com.puggicorn.perseverance.brewing.alchemy.catalyst.BasePotionComponent;
import com.puggicorn.perseverance.brewing.alchemy.converter.ConverterData;
import com.puggicorn.perseverance.brewing.alchemy.reagent.ReagentEffectInstance;
import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import com.puggicorn.perseverance.brewing.core.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlchemyPipeline {

    // Step 1: Water Bottle + Catalyst -> Potion Base

    private static boolean isCatalyst(ItemStack ingredient) {
        return ModAlchemyRegistry.CATALYSTS.getData(ingredient).isPresent();
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

        ModAlchemyRegistry.CATALYSTS.getData(catalyst).ifPresent(data -> {
            result.set(ModDataComponents.BASE_POTION_TYPE.get(),
                    new BasePotionComponent(data.baseStrategy(), data.baseID(), data.color()));
        });

        return result;
    }


    // Step 2: Potion Base + Reagent -> Potion with Effects

    private static boolean isReagent(Level level, ItemStack ingredient) {
        return ModAlchemyRegistry.REAGENTS.getData(ingredient).isPresent();
    }

    private static boolean canApplyReagent(Level level, ItemStack potion, ItemStack reagent) {
        if (!potion.is(Items.POTION)) return false;
        if (!potion.has(ModDataComponents.BASE_POTION_TYPE.get())) return false;

        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);

        if (contents != null && contents.hasEffects()) {
            return false;
        }
        return true;
    }

    private static ItemStack applyReagent(Level level, ItemStack potion, ItemStack reagent) {
        ItemStack result = potion.copyWithCount(1);

        var baseComponent = potion.get(ModDataComponents.BASE_POTION_TYPE.get());
        if (baseComponent == null) return result;

        String activebaseStrategy = baseComponent.baseStrategy();

        // Determines what effects are actually injected. See: BaseExtractionStrategy.Java
        BaseExtractionStrategy strategy = BaseExtractionStrategy.find(activebaseStrategy);

        // Processes data injection
        ModAlchemyRegistry.REAGENTS.getData(reagent).ifPresent(data -> {
            PotionContents vanillaContents = result.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            List<MobEffectInstance> updatedEffects = new ArrayList<>(vanillaContents.customEffects());
            List<ReagentEffectInstance> filteredPayloads = strategy.extract(data.effects());

            // Injects the effects from the Reagent entry.
            for (ReagentEffectInstance entry : filteredPayloads) {
                updatedEffects.add(new MobEffectInstance(entry.effect(), entry.duration(), entry.amplifier()));
            }

            int mergedColor = PotionContents.getColor(updatedEffects);

            result.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(Optional.empty(), Optional.of(mergedColor), updatedEffects)
            );
        });

        return result;
    }


    // Step 3: Potion with Effects + Converter -> Potion with new Effects [Optional]

    private static boolean isConverter(Level level, ItemStack ingredient) {
        return ModAlchemyRegistry.CONVERTERS.getData(ingredient).isPresent();
    }

    private static boolean canApplyConverter(Level level, ItemStack potion, ItemStack converter) {
        if (!potion.is(Items.POTION) && !potion.is(Items.SPLASH_POTION) && !potion.is(Items.LINGERING_POTION)) {
            return false;
        }

        if (!potion.has(ModDataComponents.BASE_POTION_TYPE.get())) return false;

        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.hasEffects()) return false;

        var converterData = ModAlchemyRegistry.CONVERTERS.getData(converter);
        if (converterData.isEmpty()) {
            PerseveranceBrewingMod.LOGGER.info("ALCHEMY DEBUG: Item [{}] is NOT registered in alchemy/converters!", converter.getItem());
            return false;
        }

        ConverterData data = converterData.get();

        for (MobEffectInstance activeEffect : contents.customEffects()) {
            if (data.canConvert(activeEffect.getEffect())) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack applyConverter(Level level, ItemStack potion, ItemStack converter) {

        ItemStack result = potion.copyWithCount(1);

        ModAlchemyRegistry.CONVERTERS.getData(converter).ifPresent(converterData -> {
            PotionContents vanillaContents = result.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            List<MobEffectInstance> currentEffects = vanillaContents.customEffects();
            List<MobEffectInstance> mutatedEffects = new java.util.ArrayList<>();

            for (MobEffectInstance activeEffect : currentEffects) {
                var originalHolder = activeEffect.getEffect();

                if (converterData.canConvert(originalHolder)) {

                    ConverterData.ConversionTarget conversion = converterData.getConversion(originalHolder);
                    var mutatedHolder = conversion.target();

                    int newDuration = (int) Math.max(1, activeEffect.getDuration() * conversion.durationMultiplier());
                    if (mutatedHolder.value().isInstantenous()) {
                        newDuration = 6;
                    }

                    int currentLevel = activeEffect.getAmplifier() + 1;
                    int newAmplifier = (int) (currentLevel * conversion.amplifierMultiplier()) - 1;
                    if (mutatedHolder.is(ModTags.NON_SCALING)) {
                        newAmplifier = 0;
                    }
                    newAmplifier = net.minecraft.util.Mth.clamp(newAmplifier, 0, 255);

                    mutatedEffects.add(new MobEffectInstance(mutatedHolder, newDuration, newAmplifier));
                } else {
                    mutatedEffects.add(activeEffect);
                }
            }

            int mergedColor = PotionContents.getColor(mutatedEffects);

            result.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(Optional.empty(), Optional.of(mergedColor), mutatedEffects)
            );

            result.set(DataComponents.CUSTOM_NAME, AlchemyNameEngine.getDynamicName(result));
        });

        return result;
    }


    // Step 4: Apply an additive -> potion effects change stats

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
            if (potionStack.isEmpty()) continue;

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
