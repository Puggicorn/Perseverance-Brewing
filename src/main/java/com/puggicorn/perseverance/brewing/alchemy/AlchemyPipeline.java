package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import com.puggicorn.perseverance.brewing.alchemy.additive.AdditiveComponent;
import com.puggicorn.perseverance.brewing.alchemy.catalyst.BasePotionComponent;
import com.puggicorn.perseverance.brewing.alchemy.converter.ConverterData;
import com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierData;
import com.puggicorn.perseverance.brewing.alchemy.reagent.ReagentEffectInstance;
import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import com.puggicorn.perseverance.brewing.core.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
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
        if (!isPotion(potion)) return false;

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


    // Step 3: Potion with Effects + Converter -> Potion with new Effects

    private static boolean isConverter(Level level, ItemStack ingredient) {
        return ModAlchemyRegistry.CONVERTERS.getData(ingredient).isPresent();
    }

    private static boolean canApplyConverter(Level level, ItemStack potion, ItemStack converter) {
        if (!isPotion(potion)) return false;
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

        ModAlchemyRegistry.CONVERTERS.getData(converter).ifPresent(data -> {
            PotionContents vanillaContents = result.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            List<MobEffectInstance> currentEffects = vanillaContents.customEffects();

            java.util.Map<Holder<MobEffect>, MobEffectInstance> pooledEffects = new java.util.LinkedHashMap<>();

            for (MobEffectInstance activeEffect : currentEffects) {
                Holder<MobEffect> targetHolder;
                int targetDuration;
                int targetAmplifier;

                var originalHolder = activeEffect.getEffect();

                // Converts an effect if applicable
                if (data.canConvert(originalHolder)) {
                    ConverterData.ConversionTarget conversion = data.getConversion(originalHolder);
                    targetHolder = conversion.target();

                    long rawDuration = (long) (activeEffect.getDuration() * conversion.durationMultiplier());
                    targetDuration = clampDuration(rawDuration, targetHolder);

                    int currentLevel = activeEffect.getAmplifier() + 1;
                    int rawAmplifier = (int) (currentLevel * conversion.amplifierMultiplier()) - 1;
                    targetAmplifier = clampAmplifier(rawAmplifier, targetHolder);
                } else {
                    targetHolder = originalHolder;
                    targetDuration = activeEffect.getDuration();
                    targetAmplifier = activeEffect.getAmplifier();
                }

                // Combines duplicate effects, making them stronger!
                if (pooledEffects.containsKey(targetHolder)) {
                    MobEffectInstance existingInstance = pooledEffects.get(targetHolder);

                    int collapsedDuration = existingInstance.getDuration();
                    int collapsedAmplifier = existingInstance.getAmplifier();

                    // Upgrade the amplifier if applicable
                    if (!targetHolder.is(ModTags.NON_SCALING) && existingInstance.getAmplifier() < MAX_AMPLIFIER) {
                        int bumpedAmplifier = Math.max(existingInstance.getAmplifier(), targetAmplifier) + 1;
                        collapsedAmplifier = clampAmplifier(bumpedAmplifier, targetHolder);
                    }
                    // Fallback to lengthening duration if the tier cannot scale or is maxed
                    else {
                        long extendedDuration = (long) existingInstance.getDuration() + targetDuration;
                        collapsedDuration = clampDuration(extendedDuration, targetHolder);
                    }

                    pooledEffects.put(targetHolder, new MobEffectInstance(targetHolder, collapsedDuration, collapsedAmplifier));
                } else {
                    pooledEffects.put(targetHolder, new MobEffectInstance(targetHolder, targetDuration, targetAmplifier));
                }
            }

            List<MobEffectInstance> finalEffects = new java.util.ArrayList<>(pooledEffects.values());

            int mergedColor = PotionContents.getColor(finalEffects);

            result.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(Optional.empty(), Optional.of(mergedColor), finalEffects)
            );

            result.set(DataComponents.CUSTOM_NAME, AlchemyNameEngine.getDynamicName(result));
        });

        return result;
    }

    // Step 4: Apply an additive -> potion effects change stats

    private static boolean isAdditive(ItemStack ingredient) {
        return ModAlchemyRegistry.ADDITIVES.getData(ingredient).isPresent();
    }

    private static boolean canApplyAdditive(ItemStack potion, ItemStack additive) {
        if (!isPotion(potion)) return false;
        if (!potion.has(ModDataComponents.BASE_POTION_TYPE.get())) return false;

        if (potion.has(ModDataComponents.ADDITIVE_COMPONENT.get())) {
            return false;
        }

        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.hasEffects()) return false;

        return ModAlchemyRegistry.ADDITIVES.getData(additive).isPresent();
    }

    private static ItemStack applyAdditive(ItemStack potion, ItemStack additive) {
        ItemStack result = potion.copyWithCount(1);

        ModAlchemyRegistry.ADDITIVES.getData(additive).ifPresent(data -> {
            PotionContents vanillaContents = result.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            List<MobEffectInstance> currentEffects = vanillaContents.customEffects();
            List<MobEffectInstance> upgradedEffects = new java.util.ArrayList<>();

            for (MobEffectInstance activeEffect : currentEffects) {
                var effectHolder = activeEffect.getEffect();

                long rawDuration = (long) (activeEffect.getDuration() * data.durationMultiplier()) + data.durationFlatBonus();
                int newDuration = clampDuration(rawDuration, effectHolder);

                int currentLevel = activeEffect.getAmplifier() + 1;
                int targetLevel = currentLevel + data.amplifierIncrease();

                int limitedLevel = Math.min(targetLevel, data.maxAmplifierLimit() + 1);
                int newAmplifier = clampAmplifier(limitedLevel - 1, effectHolder);

                upgradedEffects.add(new MobEffectInstance(effectHolder, newDuration, newAmplifier)); // Maybe add special additives in the future?
            }

            int mergedColor = PotionContents.getColor(upgradedEffects);

            result.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(Optional.empty(), Optional.of(mergedColor), upgradedEffects)
            );

            result.set(ModDataComponents.ADDITIVE_COMPONENT.get(), new AdditiveComponent(data.additiveID()));

            result.set(DataComponents.CUSTOM_NAME, AlchemyNameEngine.getDynamicName(result));
        });
        return result;
    }


    // Step 5: Apply modifiers -> Potion to Splash, Lingering, Etc

    private static boolean isModifier(ItemStack ingredient) {
        return ModAlchemyRegistry.MODIFIERS.getData(ingredient).isPresent();
    }

    private static boolean canApplyModifier(ItemStack potion, ItemStack modifier) {
        if (!isPotion(potion)) return false;
        if (!potion.has(ModDataComponents.BASE_POTION_TYPE.get())) return false;

        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.hasEffects()) return false;

        var modifierData = ModAlchemyRegistry.MODIFIERS.getData(modifier);
        if (modifierData.isEmpty()) return false;

        ModifierData data = modifierData.get();

        if (potion.is(data.targetItem())) {
            return false;
        }

        return data.isValidInput(potion.getItem());
    }
    private static ItemStack applyModifier(ItemStack potion, ItemStack modifier) {
        var modifierData = ModAlchemyRegistry.MODIFIERS.getData(modifier);
        if (modifierData.isEmpty()) return potion;

        ModifierData data = modifierData.get();

        ItemStack result = new ItemStack(data.targetItem(), 1);

        if (potion.has(ModDataComponents.BASE_POTION_TYPE.get())) {
            result.set(ModDataComponents.BASE_POTION_TYPE.get(), potion.get(ModDataComponents.BASE_POTION_TYPE.get()));
        }
        if (potion.has(ModDataComponents.ADDITIVE_COMPONENT.get())) {
            result.set(ModDataComponents.ADDITIVE_COMPONENT.get(), potion.get(ModDataComponents.ADDITIVE_COMPONENT.get()));
        }

        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            List<MobEffectInstance> scaledEffects = new ArrayList<>();

            for (MobEffectInstance instance : contents.customEffects()) {
                int newDuration = Math.max(1, (int) Math.round(instance.getDuration() * data.durationMultiplier()));

                scaledEffects.add(new MobEffectInstance(
                        instance.getEffect(),
                        newDuration,
                        instance.getAmplifier(),
                        instance.isAmbient(),
                        instance.isVisible(),
                        instance.showIcon()
                ));
            }

            result.set(DataComponents.POTION_CONTENTS, new PotionContents(
                    contents.potion(),
                    contents.customColor(),
                    scaledEffects
            ));
        }

        result.set(DataComponents.CUSTOM_NAME, AlchemyNameEngine.getDynamicName(result));

        return result;
    }

    // Helper functions

    private static final int MAX_AMPLIFIER = 255;
    private static final int MAX_DURATION_TICKS = 72000; // 1 Hour absolute safety cap
    private static final int INSTANT_DURATION_TICKS = 7; // Keeping at 7 makes the saturation effect apply like stew

    private static int clampAmplifier(int rawAmplifier, Holder<MobEffect> effectHolder) {
        if (effectHolder.is(ModTags.NON_SCALING)) {
            return 0;
        }
        return net.minecraft.util.Mth.clamp(rawAmplifier, 0, MAX_AMPLIFIER);
    }

    private static int clampDuration(long rawDuration, Holder<MobEffect> effectHolder) {
        if (effectHolder.value().isInstantenous()) {
            return INSTANT_DURATION_TICKS;
        }
        return (int) net.minecraft.util.Mth.clamp(rawDuration, 1, MAX_DURATION_TICKS);
    }

    // Brewing checks
    private static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

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
