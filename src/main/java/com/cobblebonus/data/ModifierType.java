package com.cobblebonus.data;

public enum ModifierType {
    SHINY("shiny"),
    CAPTURE("capture");

    private final String displayName;

    ModifierType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
