package com.cobblebonus;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CobbleBonusConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MAX_EFFECTIVE_SHINY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_EFFECTIVE_CAPTURE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_CATCH_RATE;
    public static final ModConfigSpec.BooleanValue DEBUG_CAPTURE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("caps");

        MAX_EFFECTIVE_SHINY_MULTIPLIER = builder
            .comment("Maximum effective multiplier for shiny chance.")
            .defineInRange("maxEffectiveShinyMultiplier", 1000.0D, 0.01D, 10000.0D);
        MAX_EFFECTIVE_CAPTURE_MULTIPLIER = builder
            .comment("Maximum effective multiplier for capture catch rate.")
            .defineInRange("maxEffectiveCaptureMultiplier", 100.0D, 0.01D, 10000.0D);
        MAX_CATCH_RATE = builder
            .comment("Maximum catch rate value after multiplier is applied.")
            .defineInRange("maxCatchRate", 255.0D, 1.0D, 100000.0D);

        builder.pop();
        builder.push("debug");

        DEBUG_CAPTURE = builder
            .comment("Enable debug logging for capture multiplier calculations.")
            .define("debugCapture", false);

        builder.pop();
        SPEC = builder.build();
    }

    private CobbleBonusConfig() {
    }
}
