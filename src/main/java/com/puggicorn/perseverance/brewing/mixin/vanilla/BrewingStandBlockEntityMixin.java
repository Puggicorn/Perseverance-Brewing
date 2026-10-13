package com.puggicorn.perseverance.brewing.mixin.vanilla;

import com.puggicorn.perseverance.brewing.alchemy.AlchemyPipeline;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Unique
    private BrewingStandBlockEntity perseverance_brewing$self() {
        return (BrewingStandBlockEntity) (Object) this;
    }

    @Unique
    private static Level perseverance_brewing$levelOf(BrewingStandBlockEntity be) {
        return be.getLevel();
    }

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/core/NonNullList;)Z"
            )
    )

    private static boolean perseverance_brewing$checkBrewable(PotionBrewing brewing, NonNullList<ItemStack> items, Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, BrewingStandBlockEntity brewingStand) {

        if (AlchemyPipeline.canProcess(items)) {
            return true;
        }

        // Vanilla recipe fallback
        return BrewingStandBlockEntityMixinInvoker.isBrewable(brewing, items);
    }

    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void perseverance_brewing$doBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        PotionBrewing brewing = level.potionBrewing();

        if (!AlchemyPipeline.canProcess(items)) {
            return;
        }

        AlchemyPipeline.executeBrewCycle(items);

        ItemStack ingredient = items.get(3);
        if (ingredient.hasCraftingRemainingItem()) {
            ItemStack remainder = ingredient.getCraftingRemainingItem();
            ingredient.shrink(1);
            if (ingredient.isEmpty()) {
                ingredient = remainder;
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
            }
        } else {
            ingredient.shrink(1);
        }
        items.set(3, ingredient);

        level.levelEvent(1035, pos, 0);
        EventHooks.onPotionBrewed(items);
        ci.cancel();
    }

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void perseverance_brewing$canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 3) {
            Level level = perseverance_brewing$levelOf(perseverance_brewing$self());
            if (level != null && AlchemyPipeline.isValidPipelineIngredient(stack)) {
                cir.setReturnValue(true);
            }
        }
    }
}
