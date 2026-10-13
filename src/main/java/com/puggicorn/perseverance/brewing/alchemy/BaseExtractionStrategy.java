package com.puggicorn.perseverance.brewing.alchemy;

import com.puggicorn.perseverance.brewing.alchemy.reagent.ReagentEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Extraction logic for each "Base" potion
public enum BaseExtractionStrategy {
    // Outputs the very first effect in an ingredient.
    PURE("pure") {
        @Override
        public List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available) {
            if (available.isEmpty()) return List.of();
            return List.of(available.get(0));
        }
    },

    // Outputs the first positive effect it finds.
    POSITIVE("positive") {
        @Override
        public List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available) {
            return available.stream()
                    .filter(e -> e.effect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                    .findFirst()
                    .map(List::of)
                    .orElse(List.of());
        }
    },

    // Outputs the first negative effect it finds.
    NEGATIVE("negative") {
        @Override
        public List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available) {
            return available.stream()
                    .filter(e -> e.effect().value().getCategory() == MobEffectCategory.HARMFUL)
                    .findFirst()
                    .map(List::of)
                    .orElse(List.of());
        }
    },

    // Outputs the first two effects it finds.
    DOUBLE("double") {
        @Override
        public List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available) {
            return safeSubList(available, 2); // 💡 Uses the shared helper below!
        }
    },

    // Outputs the first three effects it finds.
    TRIPLE("triple") {
        @Override
        public List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available) {
            return safeSubList(available, 3); // 💡 Uses the shared helper below!
        }
    },

    // Outputs 3 random effects from the effect list.
    RANDOM("random") {
        @Override
        public List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available) {
            if (available.isEmpty()) return List.of();
            List<ReagentEffectInstance> shuffled = new ArrayList<>(available);
            Collections.shuffle(shuffled);
            return safeSubList(shuffled, 3);  // 💡 Uses the shared helper below!
        }
    };

    private static List<ReagentEffectInstance> safeSubList(List<ReagentEffectInstance> list, int limit) {
        return list.subList(0, Math.min(limit, list.size()));
    }

    private final String baseID;

    BaseExtractionStrategy(String baseID) {
        this.baseID = baseID;
    }

    public String getBaseID() {
        return this.baseID;
    }

    public abstract List<ReagentEffectInstance> extract(List<ReagentEffectInstance> available);

    public static BaseExtractionStrategy find(String baseId) {
        for (BaseExtractionStrategy strategy : values()) {
            if (strategy.getBaseID().equalsIgnoreCase(baseId)) {
                return strategy;
            }
        }
        return PURE;
    }
}
