package com.cobblebonus;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.config.ModConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(CobbleBonus.MOD_ID)
public final class CobbleBonus {
    public static final String MOD_ID = "cobblebonus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobbleBonus() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, CobbleBonusConfig.SPEC);
        CobbleBonusCobblemonHooks.register();
        NeoForge.EVENT_BUS.register(new CobbleBonusCommands());
    }
}
