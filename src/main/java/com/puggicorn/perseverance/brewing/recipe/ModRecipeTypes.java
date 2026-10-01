package com.puggicorn.perseverance.brewing.recipe;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, PerseveranceBrewingMod.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EffectExtractionRecipe>> EFFECT_EXTRACTION = RECIPE_TYPES.register("effect_extraction",
        () -> RecipeType.simple(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(PerseveranceBrewingMod.MODID, "effect_extraction")));

    private ModRecipeTypes() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }
}
