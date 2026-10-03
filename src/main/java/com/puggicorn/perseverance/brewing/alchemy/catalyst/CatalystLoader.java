package com.puggicorn.perseverance.brewing.alchemy.catalyst;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CatalystLoader extends SimpleJsonResourceReloadListener{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, CatalystData> CATALYSTS = new HashMap<>();

    public CatalystLoader() {
        super(GSON, "alchemy/catalysts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        CATALYSTS.clear();
        object.forEach((location, json) -> {
            CatalystData.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> LOGGER.error("Failed to parse catalyst json {}: {}", location, error))
                    .ifPresent(data -> CATALYSTS.put(location, data));
        });
        LOGGER.info("Loaded {} ingredient catalysts from datapacks.", CATALYSTS.size());
    }

    public static Optional<CatalystData> getCatalyst(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return CATALYSTS.values().stream()
                .filter(catalystData -> catalystData.catalyst().test(stack))
                .findFirst();
    }
}
