package com.puggicorn.perseverance.brewing.mixin.vanilla;

import com.puggicorn.perseverance.brewing.alchemy.AlchemyPipeline;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$IngredientsSlot")
public class BrewingStandMenuMixin {
    @Inject(method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void perseverance_brewing$allowPipelineIngredients(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (AlchemyPipeline.isValidPipelineIngredient(stack)) {
            cir.setReturnValue(true);
        }
    }
}
