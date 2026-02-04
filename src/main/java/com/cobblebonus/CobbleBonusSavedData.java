package com.cobblebonus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class CobbleBonusSavedData extends SavedData {
    public static final String DATA_NAME = "cobblebonus_modifiers";

    private final Map<UUID, PlayerModifierData> players = new LinkedHashMap<>();

    public Map<UUID, PlayerModifierData> getPlayers() {
        return players;
    }

    public PlayerModifierData getOrCreate(UUID playerId) {
        return players.computeIfAbsent(playerId, id -> new PlayerModifierData());
    }

    public static Factory<CobbleBonusSavedData> factory() {
        return new Factory<>(CobbleBonusSavedData::new, CobbleBonusSavedData::load, DataFixTypes.LEVEL);
    }

    private static CobbleBonusSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CobbleBonusSavedData data = new CobbleBonusSavedData();
        CompoundTag playersTag = tag.getCompound("players");
        for (String key : playersTag.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            CompoundTag playerTag = playersTag.getCompound(key);
            PlayerModifierData modifierData = new PlayerModifierData();
            readModifierList(playerTag, "shiny", modifierData.getShinyModifiers());
            readModifierList(playerTag, "capture", modifierData.getCaptureModifiers());
            data.players.put(playerId, modifierData);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerModifierData> entry : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.put("shiny", writeModifierList(entry.getValue().getShinyModifiers()));
            playerTag.put("capture", writeModifierList(entry.getValue().getCaptureModifiers()));
            playersTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("players", playersTag);
        return tag;
    }

    private static void readModifierList(
        CompoundTag playerTag,
        String key,
        Map<UUID, ModifierEntry> target
    ) {
        ListTag listTag = playerTag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag modifierTag = listTag.getCompound(i);
            ModifierEntry entry = ModifierEntry.fromTag(modifierTag);
            target.put(entry.getId(), entry);
        }
    }

    private static ListTag writeModifierList(Map<UUID, ModifierEntry> entries) {
        ListTag listTag = new ListTag();
        for (ModifierEntry entry : entries.values()) {
            listTag.add(entry.toTag());
        }
        return listTag;
    }
}
