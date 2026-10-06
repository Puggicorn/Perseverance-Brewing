package com.puggicorn.perseverance.brewing.alchemy.additive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Marks that a potion has received a dynamic alchemical additive upgrade.
 */
public record AdditiveComponent(String additiveId) {

    public static final Codec<AdditiveComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("additive_id").forGetter(AdditiveComponent::additiveId)
            ).apply(instance, AdditiveComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AdditiveComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AdditiveComponent::additiveId,
            AdditiveComponent::new
    );
}
