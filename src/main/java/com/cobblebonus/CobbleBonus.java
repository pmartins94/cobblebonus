package com.cobblebonus;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.config.ModConfig;

@Mod(CobbleBonus.MOD_ID)
public final class CobbleBonus {
    public static final String MOD_ID = "cobblebonus";

    public CobbleBonus() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, CobbleBonusConfig.SPEC);
        NeoForge.EVENT_BUS.register(new CobbleBonusEvents());
        NeoForge.EVENT_BUS.register(new CobbleBonusCommands());
    }
}
