package com.cobblebonus.config;

import net.neoforged.neoforge.common.ForgeConfigSpec;

public final class CobbleBonusConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue SHINY_PLAYER_RANGE;
    public static final ForgeConfigSpec.DoubleValue MAX_EFFECTIVE_SHINY_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MAX_EFFECTIVE_CAPTURE_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("CobbleBonus server configuration").push("server");

        SHINY_PLAYER_RANGE = builder
            .comment("Search radius around a spawn to find the nearest eligible player.")
            .defineInRange("shinyPlayerRange", 48.0, 0.0, 256.0);
        MAX_EFFECTIVE_SHINY_MULTIPLIER = builder
            .comment("Upper cap for the effective shiny multiplier.")
            .defineInRange("maxEffectiveShinyMultiplier", 10.0, 0.0, 1000.0);
        MAX_EFFECTIVE_CAPTURE_MULTIPLIER = builder
            .comment("Upper cap for the effective capture multiplier.")
            .defineInRange("maxEffectiveCaptureMultiplier", 2.0, 0.0, 1000.0);

        builder.pop();
        SPEC = builder.build();
    }

    private CobbleBonusConfig() {
    }
}
