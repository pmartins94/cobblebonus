package com.cobblebonus;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokeBallCaptureCalculatedEvent;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.jvm.functions.Function3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import com.cobblemon.mod.common.Cobblemon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

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

        CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.subscribe(event -> {
            onCaptureCalculated(event);
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

    private static void onCaptureCalculated(PokeBallCaptureCalculatedEvent event) {
        if (!(event.getThrower() instanceof ServerPlayer player)) {
            return;
        }
        if (!CobbleBonusConfig.ENABLE_CAPTURE_REROLLS.get()) {
            return;
        }
        double multiplier = ModifierManager.getEffectiveCaptureMultiplier(player);
        if (multiplier <= 1.0D) {
            return;
        }
        Object initialResult = event.getCaptureResult();
        if (isCaptureSuccessful(initialResult)) {
            return;
        }
        int totalAttempts = getTotalAttempts(multiplier, player);
        int maxAttempts = CobbleBonusConfig.MAX_CAPTURE_ATTEMPTS.get();
        totalAttempts = Math.min(totalAttempts, Math.max(1, maxAttempts));
        int rerolls = totalAttempts - 1;
        if (rerolls <= 0) {
            return;
        }
        LivingEntity thrower = event.getThrower();
        EmptyPokeBallEntity pokeBallEntity = event.getPokeBallEntity();
        PokemonEntity pokemonEntity = event.getPokemonEntity();
        for (int attempt = 0; attempt < rerolls; attempt++) {
            Object rerollResult = processCapture(thrower, pokeBallEntity, pokemonEntity);
            if (isCaptureSuccessful(rerollResult)) {
                setCaptureResult(event, rerollResult);
                if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
                    CobbleBonus.LOGGER.info(
                        "Capture reroll succeeded: player={} attempt={}/{} multiplier={}",
                        player.getName().getString(),
                        attempt + 2,
                        totalAttempts,
                        multiplier
                    );
                }
                break;
            }
        }
    }

    private static int getTotalAttempts(double multiplier, ServerPlayer player) {
        int baseRolls = (int) Math.floor(multiplier);
        if (baseRolls < 1) {
            baseRolls = 1;
        }
        double fractional = multiplier - baseRolls;
        int totalAttempts = baseRolls;
        if (fractional > 0.0D && player.getRandom().nextDouble() < fractional) {
            totalAttempts += 1;
        }
        return totalAttempts;
    }

    private static Object processCapture(
        LivingEntity thrower,
        EmptyPokeBallEntity pokeBallEntity,
        PokemonEntity pokemonEntity
    ) {
        try {
            Object calculator = Cobblemon.INSTANCE.getConfig().getCaptureCalculator();
            Method processCapture = calculator.getClass().getMethod(
                "processCapture",
                LivingEntity.class,
                EmptyPokeBallEntity.class,
                PokemonEntity.class
            );
            return processCapture.invoke(calculator, thrower, pokeBallEntity, pokemonEntity);
        } catch (ReflectiveOperationException exception) {
            Object fallback = tryFallbackCaptureCalculator(thrower, pokeBallEntity, pokemonEntity, exception);
            if (fallback != null) {
                return fallback;
            }
            if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
                CobbleBonus.LOGGER.warn("Failed to compute capture reroll.", exception);
            }
            return null;
        }
    }

    private static Object tryFallbackCaptureCalculator(
        LivingEntity thrower,
        EmptyPokeBallEntity pokeBallEntity,
        PokemonEntity pokemonEntity,
        ReflectiveOperationException originalException
    ) {
        try {
            Class<?> fallbackClass = Class.forName(
                "com.cobblemon.mod.common.api.pokeball.CobblemonCaptureCalculator"
            );
            Method processCapture = fallbackClass.getMethod(
                "processCapture",
                LivingEntity.class,
                EmptyPokeBallEntity.class,
                PokemonEntity.class
            );
            return processCapture.invoke(null, thrower, pokeBallEntity, pokemonEntity);
        } catch (ReflectiveOperationException fallbackException) {
            if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
                CobbleBonus.LOGGER.warn(
                    "Failed to invoke fallback capture calculator after primary error.",
                    originalException
                );
            }
            return null;
        }
    }

    private static boolean isCaptureSuccessful(Object captureResult) {
        if (captureResult == null) {
            return false;
        }
        for (String methodName : new String[] {"isSuccessful", "wasSuccessful", "isSuccess"}) {
            try {
                Method method = captureResult.getClass().getMethod(methodName);
                Object value = method.invoke(captureResult);
                if (value instanceof Boolean boolValue) {
                    return boolValue;
                }
            } catch (NoSuchMethodException ignored) {
                // try next
            } catch (IllegalAccessException | InvocationTargetException exception) {
                if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
                    CobbleBonus.LOGGER.warn("Failed to inspect capture result.", exception);
                }
                break;
            }
        }
        Object resultValue = readCaptureResultValue(captureResult);
        if (resultValue == null) {
            return false;
        }
        String value = String.valueOf(resultValue).toUpperCase(Locale.ROOT);
        return value.contains("SUCCESS") || value.contains("CAPTURE") || value.contains("CAUGHT");
    }

    private static Object readCaptureResultValue(Object captureResult) {
        for (String methodName : new String[] {"getCaptureResult", "getResult", "getStatus"}) {
            try {
                Method method = captureResult.getClass().getMethod(methodName);
                return method.invoke(captureResult);
            } catch (NoSuchMethodException ignored) {
                // try next
            } catch (IllegalAccessException | InvocationTargetException exception) {
                if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
                    CobbleBonus.LOGGER.warn("Failed to read capture result status.", exception);
                }
                return null;
            }
        }
        return null;
    }

    private static void setCaptureResult(PokeBallCaptureCalculatedEvent event, Object captureResult) {
        if (captureResult == null) {
            return;
        }
        try {
            Method target = null;
            for (Method method : event.getClass().getMethods()) {
                if (method.getName().equals("setCaptureResult") && method.getParameterCount() == 1) {
                    target = method;
                    break;
                }
            }
            if (target != null) {
                target.invoke(event, captureResult);
            }
        } catch (ReflectiveOperationException exception) {
            if (CobbleBonusConfig.DEBUG_CAPTURE.get()) {
                CobbleBonus.LOGGER.warn("Failed to set capture reroll result.", exception);
            }
        }
    }
}
