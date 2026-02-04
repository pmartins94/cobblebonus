package com.cobblebonus;

import com.cobblebonus.command.CobbleBonusCommands;
import com.cobblebonus.config.CobbleBonusConfig;
import com.cobblebonus.event.CobbleBonusEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CobbleBonus.MOD_ID)
public final class CobbleBonus {
    public static final String MOD_ID = "cobblebonus";

    public CobbleBonus(final IEventBus modBus) {
        ModLoadingContext.get().registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, CobbleBonusConfig.SPEC);
        NeoForge.EVENT_BUS.register(CobbleBonusEvents.class);
        NeoForge.EVENT_BUS.addListener(CobbleBonusCommands::onRegisterCommands);
    }
}
