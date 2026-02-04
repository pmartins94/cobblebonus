package com.cobblebonus;

import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.jvm.functions.Function3;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

public final class CobbleBonusEvents {

    @SubscribeEvent
    public void onCatchRate(PokemonCatchRateEvent event) {
        if (!(event.getThrower() instanceof ServerPlayer player)) {
            return;
        }
        double multiplier = ModifierManager.getEffectiveCaptureMultiplier(player);
        float updated = (float) (event.getCatchRate() * multiplier);
        float capped = (float) Math.min(updated, CobbleBonusConfig.MAX_CATCH_RATE.get());
        event.setCatchRate(capped);
    }

    @SubscribeEvent
    public void onShinyChance(ShinyChanceCalculationEvent event) {
        event.addModificationFunction((Function3<Float, ServerPlayer, Pokemon, Float>)
            (currentChance, player, pokemon) -> {
                if (player == null) {
                    return currentChance;
                }
                double multiplier = ModifierManager.getEffectiveShinyMultiplier(player);
                float updated = (float) (currentChance * multiplier);
                float capped = Math.min(updated, 1.0F);
                return Math.max(0.0F, capped);
            }
        );
    }
}
