package com.puggicorn.perseverance.brewing;

import com.mojang.logging.LogUtils;
import com.puggicorn.perseverance.brewing.alchemy.AlchemyNameEngine;
import com.puggicorn.perseverance.brewing.alchemy.catalyst.CatalystLoader;
import com.puggicorn.perseverance.brewing.alchemy.reagent.ReagentLoader;
import com.puggicorn.perseverance.brewing.block.entity.ModBlockEntities;
import com.puggicorn.perseverance.brewing.core.*;
import com.puggicorn.perseverance.brewing.effect.glowing.GlowingVisibilityEvents;
import com.puggicorn.perseverance.brewing.effect.rage.RageEvents;
import com.puggicorn.perseverance.brewing.potion.ModPotions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PerseveranceBrewingMod.MODID)
public class PerseveranceBrewingMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "perseverance_brewing";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public PerseveranceBrewingMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModPotions.register(modEventBus);

        modEventBus.addListener(ModNetworking::registerPayloads);
        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(RageEvents.class);
        NeoForge.EVENT_BUS.register(GlowingVisibilityEvents.class);

        // Ingredient Type Loaders
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> {
            event.addListener(new CatalystLoader());
            event.addListener(new ReagentLoader());
        });

        // Item names, Tooltips, Etc
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, event -> {
            ItemStack stack = event.getItemStack();

            if (stack.has(ModDataComponents.BASE_POTION_TYPE.get())) {

                // Sets the potion name
                event.getToolTip().set(0, AlchemyNameEngine.getDynamicName(stack));

                // Sets the effect tooltip
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents == null || !contents.hasEffects()) {
                    event.getToolTip().add(1, Component.translatable("effect.none")
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        });

    }

    // Add the centrifuge and breeze powder to their creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.CENTRIFUGE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BREEZE_POWDER);
        }
    }
}
