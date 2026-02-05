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
        float original = event.getCatchRate();
        double multiplier = ModifierManager.getEffectiveCaptureMultiplier(player);
        float updated = (float) (original * multiplier);
        float capped = (float) Math.min(updated, CobbleBonusConfig.MAX_CATCH_RATE.get());
        event.setCatchRate(capped);
        if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
            Pokemon pokemon = event.getPokemonEntity().getPokemon();
            CobbleBonus.LOGGER.info(
                "Capture rate debug: player={} uuid={} pokemon={} lvl={} oldRate={} multiplier={} newRate={}",
                player.getName().getString(),
                player.getUUID(),
                pokemon.getSpecies().getName(),
                pokemon.getLevel(),
                original,
                multiplier,
                capped
            );
        }
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
                float updated = (float) (currentChance / multiplier);
                if (!Float.isFinite(updated) || updated < 1.0F) {
                    return 1.0F;
                }
                return updated;
            }
        );
    }
}
