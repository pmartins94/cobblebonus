package com.cobblebonus.data;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public record ModifierEntry(UUID uuid, double multiplier, @Nullable String name) {
}
