package com.puggicorn.perseverance.brewing.core;

import com.puggicorn.perseverance.brewing.PerseveranceBrewingMod;
import com.puggicorn.perseverance.brewing.menu.CentrifugeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, PerseveranceBrewingMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CentrifugeMenu>> CENTRIFUGE = MENUS.register("centrifuge",
        () -> IMenuTypeExtension.create((containerId, inventory, data) -> new CentrifugeMenu(containerId, inventory)));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
