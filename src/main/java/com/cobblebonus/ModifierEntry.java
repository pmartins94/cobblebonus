package com.cobblebonus;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;

public final class ModifierEntry {
    private final String id;
    private final double multiplier;

    public ModifierEntry(String id, double multiplier) {
        this.id = Objects.requireNonNull(id, "id");
        this.multiplier = multiplier;
    }

    public String getId() {
        return id;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putDouble("multiplier", multiplier);
        return tag;
    }

    public static ModifierEntry fromTag(CompoundTag tag) {
        String id = tag.getString("id");
        double multiplier = tag.getDouble("multiplier");
        return new ModifierEntry(id, multiplier);
    }
}
