package com.puggicorn.perseverance.brewing;

import com.puggicorn.perseverance.brewing.block.entity.ModBlockEntities;
import com.puggicorn.perseverance.brewing.client.CentrifugeRenderer;
import com.puggicorn.perseverance.brewing.client.CentrifugeScreen;
import com.puggicorn.perseverance.brewing.core.ModDataComponents;
import com.puggicorn.perseverance.brewing.core.ModMenus;
import com.puggicorn.perseverance.brewing.effect.rage.client.RagePlayerClientEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = PerseveranceBrewingMod.MODID, dist = Dist.CLIENT)
public class PerseveranceBrewingModClient {

    public PerseveranceBrewingModClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(RegisterColorHandlersEvent.Item.class, event -> {
            event.register((stack, tintIndex) -> {
                if (tintIndex == 0) {
                    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

                    if (contents != null && contents.hasEffects()) {
                        return contents.getColor();
                    }

                    if (stack.has(ModDataComponents.BASE_POTION_TYPE.get())) {
                        var baseData = stack.get(ModDataComponents.BASE_POTION_TYPE.get());
                        if (baseData != null) {
                            return (baseData.color() & 0x00FFFFFF) | 0xFF000000;
                        }
                    }

                    if (contents != null) {
                        return contents.getColor();
                    }
                }
                return -1;
            }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
        });

        modEventBus.addListener((RegisterMenuScreensEvent event) ->
            event.register(ModMenus.CENTRIFUGE.get(), CentrifugeScreen::new));
        modEventBus.addListener((ModelEvent.RegisterAdditional event) -> event.register(CentrifugeRenderer.ROTOR_MODEL));
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
            event.registerBlockEntityRenderer(ModBlockEntities.CENTRIFUGE.get(), context -> new CentrifugeRenderer()));

        NeoForge.EVENT_BUS.register(RagePlayerClientEvents.class);
    }
}
