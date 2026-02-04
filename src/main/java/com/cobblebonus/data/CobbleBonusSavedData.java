package com.cobblebonus.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public final class CobbleBonusSavedData extends SavedData {
    private static final String DATA_NAME = "cobblebonus";
    private final Map<UUID, PlayerModifiers> players = new HashMap<>();

    public CobbleBonusSavedData() {
    }

    public CobbleBonusSavedData(CompoundTag tag) {
        CompoundTag playersTag = tag.getCompound("players");
        for (String key : playersTag.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            CompoundTag playerTag = playersTag.getCompound(key);
            PlayerModifiers modifiers = new PlayerModifiers();
            modifiers.read(playerTag);
            players.put(playerId, modifiers);
        }
    }

    public static CobbleBonusSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CobbleBonusSavedData::new, CobbleBonusSavedData::new, DATA_NAME);
    }

    public Map<UUID, ModifierEntry> getModifiers(UUID playerId, ModifierType type) {
        return players.computeIfAbsent(playerId, ignored -> new PlayerModifiers()).getModifiers(type);
    }

    public void upsertModifier(UUID playerId, ModifierType type, ModifierEntry entry) {
        players.computeIfAbsent(playerId, ignored -> new PlayerModifiers()).upsert(type, entry);
        setDirty();
    }

    public boolean removeModifier(UUID playerId, ModifierType type, UUID entryId) {
        PlayerModifiers modifiers = players.get(playerId);
        if (modifiers == null) {
            return false;
        }
        boolean removed = modifiers.remove(type, entryId);
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public int clearModifiers(UUID playerId, ModifierType type) {
        PlayerModifiers modifiers = players.get(playerId);
        if (modifiers == null) {
            return 0;
        }
        int count = modifiers.clear(type);
        if (count > 0) {
            setDirty();
        }
        return count;
    }

    public double getEffectiveMultiplier(UUID playerId, ModifierType type, double cap) {
        PlayerModifiers modifiers = players.get(playerId);
        if (modifiers == null) {
            return 1.0;
        }
        double product = modifiers.product(type);
        if (cap <= 0) {
            return product;
        }
        return Math.min(product, cap);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerModifiers> entry : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            entry.getValue().write(playerTag);
            playersTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("players", playersTag);
        return tag;
    }

    private static final class PlayerModifiers {
        private final Map<UUID, ModifierEntry> shiny = new HashMap<>();
        private final Map<UUID, ModifierEntry> capture = new HashMap<>();

        public Map<UUID, ModifierEntry> getModifiers(ModifierType type) {
            return type == ModifierType.SHINY ? shiny : capture;
        }

        public void upsert(ModifierType type, ModifierEntry entry) {
            getModifiers(type).put(entry.uuid(), entry);
        }

        public boolean remove(ModifierType type, UUID id) {
            return getModifiers(type).remove(id) != null;
        }

        public int clear(ModifierType type) {
            Map<UUID, ModifierEntry> map = getModifiers(type);
            int size = map.size();
            map.clear();
            return size;
        }

        public double product(ModifierType type) {
            double product = 1.0;
            for (ModifierEntry entry : getModifiers(type).values()) {
                product *= entry.multiplier();
            }
            return product;
        }

        public void read(CompoundTag tag) {
            readList(tag.getList("shiny", Tag.TAG_COMPOUND), shiny);
            readList(tag.getList("capture", Tag.TAG_COMPOUND), capture);
        }

        public void write(CompoundTag tag) {
            tag.put("shiny", writeList(shiny));
            tag.put("capture", writeList(capture));
        }

        private void readList(ListTag listTag, Map<UUID, ModifierEntry> target) {
            target.clear();
            for (Tag tag : listTag) {
                if (!(tag instanceof CompoundTag entryTag)) {
                    continue;
                }
                UUID uuid = UUID.fromString(entryTag.getString("uuid"));
                double multiplier = entryTag.getDouble("multiplier");
                String name = entryTag.contains("name") ? entryTag.getString("name") : null;
                target.put(uuid, new ModifierEntry(uuid, multiplier, name));
            }
        }

        private ListTag writeList(Map<UUID, ModifierEntry> source) {
            ListTag list = new ListTag();
            for (ModifierEntry entry : source.values()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("uuid", entry.uuid().toString());
                tag.putDouble("multiplier", entry.multiplier());
                if (entry.name() != null && !entry.name().isBlank()) {
                    tag.putString("name", entry.name());
                }
                list.add(tag);
            }
            return list;
        }
    }
}
