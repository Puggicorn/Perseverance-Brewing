package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.alchemy.additive.AdditiveData;
import com.puggicorn.perseverance.brewing.alchemy.catalyst.CatalystData;
import com.puggicorn.perseverance.brewing.alchemy.converter.ConverterData;
import com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierData;
import com.puggicorn.perseverance.brewing.alchemy.reagent.ReagentData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = "perseverance_brewing")
public class ModAlchemyRegistry {

    public static final AlchemyResourceLoader<CatalystData> CATALYSTS = new AlchemyResourceLoader<>(
            CatalystData.CODEC, "alchemy/catalysts", CatalystData::catalyst
    );

    public static final AlchemyResourceLoader<ReagentData> REAGENTS = new AlchemyResourceLoader<>(
            ReagentData.CODEC, "alchemy/reagents", ReagentData::reagent
    );

    public static final AlchemyResourceLoader<ConverterData> CONVERTERS = new AlchemyResourceLoader<>(
            ConverterData.CODEC, "alchemy/converters", ConverterData::converter
    );

    public static final AlchemyResourceLoader<AdditiveData> ADDITIVES = new AlchemyResourceLoader<>(
            AdditiveData.CODEC, "alchemy/additives", AdditiveData::additive
    );

    public static final AlchemyResourceLoader<ModifierData> MODIFIERS = new AlchemyResourceLoader<>(
            ModifierData.CODEC, "alchemy/modifiers", ModifierData::modifier
    );


    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(CATALYSTS);
        event.addListener(REAGENTS);
        event.addListener(CONVERTERS);
        event.addListener(ADDITIVES);
        event.addListener(MODIFIERS);
    }
}
