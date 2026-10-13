package com.puggicorn.perseverance.brewing.alchemy.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Marks that a potion has received a modifier.
 */
public record ModifierComponent(String modifierID) {

    public static final Codec<com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("modifier_id").forGetter(com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierComponent::modifierID)
            ).apply(instance, com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierComponent::modifierID,
            com.puggicorn.perseverance.brewing.alchemy.modifier.ModifierComponent::new
    );
}
