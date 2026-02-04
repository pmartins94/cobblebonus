package com.cobblebonus;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerModifierData {
    private final Map<String, ModifierEntry> shinyModifiers = new LinkedHashMap<>();
    private final Map<String, ModifierEntry> captureModifiers = new LinkedHashMap<>();

    public Map<String, ModifierEntry> getShinyModifiers() {
        return shinyModifiers;
    }

    public Map<String, ModifierEntry> getCaptureModifiers() {
        return captureModifiers;
    }

    public void setShinyModifier(ModifierEntry entry) {
        shinyModifiers.put(entry.getId(), entry);
    }

    public void setCaptureModifier(ModifierEntry entry) {
        captureModifiers.put(entry.getId(), entry);
    }

    public ModifierEntry removeShinyModifier(String id) {
        return shinyModifiers.remove(id);
    }

    public ModifierEntry removeCaptureModifier(String id) {
        return captureModifiers.remove(id);
    }

    public void clearShiny() {
        shinyModifiers.clear();
    }

    public void clearCapture() {
        captureModifiers.clear();
    }

    public double getEffectiveShinyMultiplier(double cap) {
        return computeMultiplier(shinyModifiers.values(), cap);
    }

    public double getEffectiveCaptureMultiplier(double cap) {
        return computeMultiplier(captureModifiers.values(), cap);
    }

    private double computeMultiplier(Collection<ModifierEntry> entries, double cap) {
        double total = 1.0D;
        for (ModifierEntry entry : entries) {
            total *= entry.getMultiplier();
            if (total > cap) {
                return cap;
            }
        }
        return Math.min(total, cap);
    }
}
