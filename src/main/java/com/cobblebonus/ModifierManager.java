package com.cobblebonus;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ModifierManager {
    private ModifierManager() {
    }

    public static CobbleBonusSavedData getData(MinecraftServer server) {
        ServerLevel level = server.overworld();
        return level.getDataStorage().computeIfAbsent(
            CobbleBonusSavedData.factory(),
            CobbleBonusSavedData.DATA_NAME
        );
    }

    public static PlayerModifierData getPlayerData(ServerPlayer player) {
        return getData(player.server).getOrCreate(player.getUUID());
    }

    public static double getEffectiveShinyMultiplier(ServerPlayer player) {
        double cap = CobbleBonusConfig.MAX_EFFECTIVE_SHINY_MULTIPLIER.get();
        return getPlayerData(player).getEffectiveShinyMultiplier(cap);
    }

    public static double getEffectiveCaptureMultiplier(ServerPlayer player) {
        double cap = CobbleBonusConfig.MAX_EFFECTIVE_CAPTURE_MULTIPLIER.get();
        return getPlayerData(player).getEffectiveCaptureMultiplier(cap);
    }

    public static void setShinyModifier(ServerPlayer player, ModifierEntry entry) {
        CobbleBonusSavedData data = getData(player.server);
        data.getOrCreate(player.getUUID()).setShinyModifier(entry);
        data.setDirty();
    }

    public static void setCaptureModifier(ServerPlayer player, ModifierEntry entry) {
        CobbleBonusSavedData data = getData(player.server);
        data.getOrCreate(player.getUUID()).setCaptureModifier(entry);
        data.setDirty();
    }

    public static boolean removeShinyModifier(ServerPlayer player, UUID id) {
        CobbleBonusSavedData data = getData(player.server);
        ModifierEntry removed = data.getOrCreate(player.getUUID()).removeShinyModifier(id);
        if (removed != null) {
            data.setDirty();
            return true;
        }
        return false;
    }

    public static boolean removeCaptureModifier(ServerPlayer player, UUID id) {
        CobbleBonusSavedData data = getData(player.server);
        ModifierEntry removed = data.getOrCreate(player.getUUID()).removeCaptureModifier(id);
        if (removed != null) {
            data.setDirty();
            return true;
        }
        return false;
    }

    public static void clearShiny(ServerPlayer player) {
        CobbleBonusSavedData data = getData(player.server);
        data.getOrCreate(player.getUUID()).clearShiny();
        data.setDirty();
    }

    public static void clearCapture(ServerPlayer player) {
        CobbleBonusSavedData data = getData(player.server);
        data.getOrCreate(player.getUUID()).clearCapture();
        data.setDirty();
    }
}
