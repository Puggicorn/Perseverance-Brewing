package com.puggicorn.perseverance.brewing.alchemy.reagent;

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

public class ReagentLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Map<ResourceLocation, ReagentData> REAGENTS = new HashMap<>();

    public ReagentLoader() {
        super(GSON, "alchemy/reagents");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        REAGENTS.clear();
        object.forEach((location, json) -> {
            ReagentData.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> LOGGER.error("Failed to parse reagent json {}: {}", location, error))
                    .ifPresent(data -> REAGENTS.put(location, data));
        });
        LOGGER.info("Loaded {} dynamic chemistry reagents from datapacks.", REAGENTS.size());
    }

    /**
     * Finds a matching reagent configuration for a given item stack.
     */
    public static Optional<ReagentData> getReagent(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return REAGENTS.values().stream()
                .filter(data -> data.reagent().test(stack))
                .findFirst();
    }
}
