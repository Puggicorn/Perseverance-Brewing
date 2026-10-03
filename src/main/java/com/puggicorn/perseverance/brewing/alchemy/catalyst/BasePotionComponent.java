package com.puggicorn.perseverance.brewing.alchemy.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BasePotionComponent(String baseID, String baseName, int color) {

    public static final Codec<BasePotionComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("base_id").forGetter(BasePotionComponent::baseID),
                    Codec.STRING.fieldOf("base_name").forGetter(BasePotionComponent::baseName),
                    Codec.INT.fieldOf("color").forGetter(BasePotionComponent::color)
            ).apply(instance, BasePotionComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BasePotionComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BasePotionComponent::baseID,
            ByteBufCodecs.STRING_UTF8, BasePotionComponent::baseName,
            ByteBufCodecs.VAR_INT, BasePotionComponent::color,
            BasePotionComponent::new
    );
}