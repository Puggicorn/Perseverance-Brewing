package com.puggicorn.perseverance.brewing.mixin.vanilla;

import com.puggicorn.perseverance.brewing.alchemy.AlchemyNameEngine;
import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

    private PotionItemMixin(Properties properties) { super(properties); }

    // Extends the getName function from PotionItem's inherited class for our own purposes.
    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(ModDataComponents.BASE_POTION_TYPE.get())) {
            return AlchemyNameEngine.getDynamicName(stack);
        }
        return super.getName(stack);
    }

    // Saturation fix
    @Inject(
            method = "finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD")
    )
    private void catchSaturationDrinking(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide()) {
            return;
        }

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {

            contents.forEachEffect(effect -> {
                if (effect.getEffect().is(MobEffects.SATURATION)) {
                    MobEffectInstance stewSaturation = new MobEffectInstance(
                            MobEffects.SATURATION,
                            effect.getDuration(),
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible(),
                            effect.showIcon()
                    );
                        entity.addEffect(stewSaturation);
                }
            });
        }
    }
}
