package com.cobblebonus.event;

import com.cobblebonus.config.CobbleBonusConfig;
import com.cobblebonus.data.CobbleBonusSavedData;
import com.cobblebonus.data.ModifierType;
import com.cobblemon.mod.common.api.events.pokeball.PokeBallCaptureCalculatedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSpawnEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

public final class CobbleBonusEvents {
    private CobbleBonusEvents() {
    }

    @SubscribeEvent
    public static void onPokemonSpawn(final PokemonSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player nearest = serverLevel.getNearestPlayer(
            event.getEntity().getX(),
            event.getEntity().getY(),
            event.getEntity().getZ(),
            CobbleBonusConfig.SHINY_PLAYER_RANGE.get(),
            player -> player != null && player.isAlive() && !player.isSpectator()
        );
        if (nearest == null) {
            return;
        }
        CobbleBonusSavedData data = CobbleBonusSavedData.get(serverLevel);
        double effective = data.getEffectiveMultiplier(nearest.getUUID(), ModifierType.SHINY, CobbleBonusConfig.MAX_EFFECTIVE_SHINY_MULTIPLIER.get());
        double baseChance = event.getShinyChance();
        double finalChance = Mth.clamp(baseChance * effective, 0.0, 1.0);
        boolean shiny = serverLevel.getRandom().nextDouble() < finalChance;
        event.setShiny(shiny);
    }

    @SubscribeEvent
    public static void onCaptureCalculated(final PokeBallCaptureCalculatedEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        CobbleBonusSavedData data = CobbleBonusSavedData.get(level);
        double effective = data.getEffectiveMultiplier(player.getUUID(), ModifierType.CAPTURE, CobbleBonusConfig.MAX_EFFECTIVE_CAPTURE_MULTIPLIER.get());
        double baseChance = event.getFinalCaptureChance();
        double finalChance = Mth.clamp(baseChance * effective, 0.0, 1.0);
        event.setFinalCaptureChance(finalChance);
    }
}
