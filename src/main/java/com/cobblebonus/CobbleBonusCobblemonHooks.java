package com.cobblebonus;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.jvm.functions.Function3;
import net.minecraft.server.level.ServerPlayer;

public final class CobbleBonusCobblemonHooks {
    private CobbleBonusCobblemonHooks() {
    }

    public static void register() {
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe(event -> {
            onCatchRate(event);
        });

        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(event -> {
            onShinyChance(event);
        });
    }

    private static void onCatchRate(PokemonCatchRateEvent event) {
        if (!(event.getThrower() instanceof ServerPlayer player)) {
            return;
        }
        double multiplier = ModifierManager.getEffectiveCaptureMultiplier(player);
        float updated = (float) (event.getCatchRate() * multiplier);
        float capped = (float) Math.min(updated, CobbleBonusConfig.MAX_CATCH_RATE.get());
        event.setCatchRate(capped);
    }

    private static void onShinyChance(ShinyChanceCalculationEvent event) {
        event.addModificationFunction((Function3<Float, ServerPlayer, Pokemon, Float>)
            (currentChance, player, pokemon) -> {
                if (player == null) {
                    return currentChance;
                }
                double multiplier = ModifierManager.getEffectiveShinyMultiplier(player);
                if (multiplier <= 0) {
                    return currentChance;
                }
                float updated = (float) (currentChance * multiplier);
                if (!Float.isFinite(updated) || updated < 0.0F) {
                    return currentChance;
                }
                return updated;
            }
        );
    }
}
