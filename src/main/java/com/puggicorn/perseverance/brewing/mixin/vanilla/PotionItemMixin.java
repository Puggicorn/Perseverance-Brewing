package com.puggicorn.perseverance.brewing.mixin.vanilla;

import com.puggicorn.perseverance.brewing.alchemy.AlchemyNameEngine;
import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;

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
}
