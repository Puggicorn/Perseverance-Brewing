package com.puggicorn.perseverance.brewing.alchemy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AlchemyResourceLoader<T> extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private final Codec<T> codec;
    private final String dataTypeName;
    private final Map<ResourceLocation, T> registryCache = new HashMap<>();
    private final Function<T, Ingredient> ingredientExtractor;

    public AlchemyResourceLoader(Codec<T> codec, String folderPath, Function<T, Ingredient> ingredientExtractor) {
        super(GSON, folderPath);
        this.codec = codec;
        this.dataTypeName = folderPath.substring(folderPath.lastIndexOf('/') + 1); // e.g., "converters"
        this.ingredientExtractor = ingredientExtractor;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        registryCache.clear();
        object.forEach((location, json) -> codec.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> LOGGER.error("Failed to parse alchemy {} json {}: {}", dataTypeName, location, error))
                .ifPresent(data -> registryCache.put(location, data)));
        LOGGER.info("Successfully loaded {} data-driven alchemy {} profiles.", registryCache.size(), dataTypeName);
    }

    public Optional<T> getData(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return registryCache.values().stream()
                .filter(data -> ingredientExtractor.apply(data).test(stack))
                .findFirst();
    }

    public void clear() {
        registryCache.clear();
    }
}