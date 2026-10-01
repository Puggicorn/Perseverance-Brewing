package com.puggicorn.perseverance.brewing.compat.jei;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import com.puggicorn.perseverance.brewing.block.ModBlocks;
import com.puggicorn.perseverance.brewing.recipe.EffectExtractionRecipe;
import com.puggicorn.perseverance.brewing.recipe.ModRecipeTypes;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

@JeiPlugin
public class PerseveranceBrewingJeiPlugin implements IModPlugin {
    public static final RecipeType<EffectExtractionRecipe> EXTRACTION =
        RecipeType.create(PerseveranceBrewingMod.MODID, "effect_extraction", EffectExtractionRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(PerseveranceBrewingMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ExtractionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level; // JEI calls this client-side after login
        if (level == null) {
            return;
        }
        List<EffectExtractionRecipe> recipes = level.getRecipeManager()
            .getAllRecipesFor(ModRecipeTypes.EFFECT_EXTRACTION.get())
            .stream()
            .map(RecipeHolder::value)
            .toList();
        registration.addRecipes(EXTRACTION, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.BREWING_STAND), EXTRACTION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CENTRIFUGE.get()), EXTRACTION);
    }
}
