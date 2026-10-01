package com.puggicorn.perseverance.brewing.compat.rei;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import com.puggicorn.perseverance.brewing.block.ModBlocks;
import com.puggicorn.perseverance.brewing.recipe.EffectExtractionRecipe;
import com.puggicorn.perseverance.brewing.recipe.ModRecipeTypes;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.world.item.Items;

@REIPluginClient
public class PerseveranceBrewingReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<ExtractionDisplay> EXTRACTION =
        CategoryIdentifier.of(PerseveranceBrewingMod.MODID, "effect_extraction");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ExtractionCategory());
        registry.addWorkstations(EXTRACTION, EntryStacks.of(Items.BREWING_STAND), EntryStacks.of(ModBlocks.CENTRIFUGE.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(EffectExtractionRecipe.class, ModRecipeTypes.EFFECT_EXTRACTION.get(), ExtractionDisplay::new);
    }
}
