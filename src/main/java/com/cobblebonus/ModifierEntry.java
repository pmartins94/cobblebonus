package com.cobblebonus;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public final class ModifierEntry {
    private final UUID id;
    private final double multiplier;
    private final String name;

    public ModifierEntry(UUID id, double multiplier, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.multiplier = multiplier;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getName() {
        return name;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putDouble("multiplier", multiplier);
        if (name != null && !name.isBlank()) {
            tag.putString("name", name);
        }
        return tag;
    }

    public static ModifierEntry fromTag(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        double multiplier = tag.getDouble("multiplier");
        String name = tag.contains("name") ? tag.getString("name") : null;
        return new ModifierEntry(id, multiplier, name);
    }
}
