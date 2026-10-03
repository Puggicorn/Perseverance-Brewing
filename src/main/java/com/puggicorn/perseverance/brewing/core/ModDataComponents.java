package com.puggicorn.perseverance.brewing.core;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import com.puggicorn.perseverance.brewing.alchemy.catalyst.BasePotionComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PerseveranceBrewingMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BasePotionComponent>> BASE_POTION_TYPE =
            COMPONENTS.register("base_potion_type", () -> DataComponentType.<BasePotionComponent>builder()
                    .persistent(BasePotionComponent.CODEC)
                    .build());

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
