package com.puggicorn.perseverance.brewing.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

public class ModTags {

    public static final TagKey<MobEffect> NON_SCALING = TagKey.create(
            Registries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath("perseverance_brewing", "non_scaling")
    );

}
