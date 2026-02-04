package com.cobblebonus.command;

import com.cobblebonus.config.CobbleBonusConfig;
import com.cobblebonus.data.CobbleBonusSavedData;
import com.cobblebonus.data.ModifierEntry;
import com.cobblebonus.data.ModifierType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.UuidArgument;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CobbleBonusCommands {
    private CobbleBonusCommands() {
    }

    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("cobblebonus")
                .requires(source -> source.hasPermission(2))
                .then(buildModifierRoot("shiny", ModifierType.SHINY))
                .then(buildModifierRoot("capture", ModifierType.CAPTURE))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildModifierRoot(String name, ModifierType type) {
        return Commands.literal(name)
            .then(Commands.literal("modifier")
                .then(Commands.literal("add")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                            .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0000001))
                                .executes(context -> addModifier(context, type, null))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                    .executes(context -> addModifier(context, type, StringArgumentType.getString(context, "name")))
                                )
                            )
                        )
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                            .executes(context -> removeModifier(context, type))
                        )
                    )
                )
                .then(Commands.literal("list")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> listModifiers(context, type))
                    )
                )
                .then(Commands.literal("clear")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> clearModifiers(context, type))
                    )
                )
            )
            .then(Commands.literal("effective")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> showEffective(context, type))
                )
            );
    }

    private static int addModifier(CommandContext<CommandSourceStack> context, ModifierType type, String name) {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        UUID uuid = UuidArgument.getUuid(context, "uuid");
        double multiplier = DoubleArgumentType.getDouble(context, "multiplier");
        ServerLevel level = target.serverLevel();
        CobbleBonusSavedData data = CobbleBonusSavedData.get(level);
        data.upsertModifier(target.getUUID(), type, new ModifierEntry(uuid, multiplier, name));
        context.getSource().sendSuccess(
            () -> Component.literal("Added " + type.getDisplayName() + " modifier " + uuid + " (" + multiplier + ") to " + target.getGameProfile().getName()),
            true
        );
        return 1;
    }

    private static int removeModifier(CommandContext<CommandSourceStack> context, ModifierType type) {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        UUID uuid = UuidArgument.getUuid(context, "uuid");
        CobbleBonusSavedData data = CobbleBonusSavedData.get(target.serverLevel());
        boolean removed = data.removeModifier(target.getUUID(), type, uuid);
        if (removed) {
            context.getSource().sendSuccess(
                () -> Component.literal("Removed " + type.getDisplayName() + " modifier " + uuid + " from " + target.getGameProfile().getName()),
                true
            );
        } else {
            context.getSource().sendFailure(Component.literal("No modifier " + uuid + " found for " + target.getGameProfile().getName()));
        }
        return removed ? 1 : 0;
    }

    private static int listModifiers(CommandContext<CommandSourceStack> context, ModifierType type) {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        CobbleBonusSavedData data = CobbleBonusSavedData.get(target.serverLevel());
        Map<UUID, ModifierEntry> modifiers = data.getModifiers(target.getUUID(), type);
        if (modifiers.isEmpty()) {
            context.getSource().sendSuccess(
                () -> Component.literal("No " + type.getDisplayName() + " modifiers for " + target.getGameProfile().getName()),
                false
            );
            return 0;
        }
        context.getSource().sendSuccess(
            () -> Component.literal(type.getDisplayName() + " modifiers for " + target.getGameProfile().getName() + ":"),
            false
        );
        for (ModifierEntry entry : modifiers.values()) {
            String label = entry.name() == null || entry.name().isBlank() ? "" : " (" + entry.name() + ")";
            context.getSource().sendSuccess(
                () -> Component.literal("- " + entry.uuid() + " x" + entry.multiplier() + label),
                false
            );
        }
        return modifiers.size();
    }

    private static int clearModifiers(CommandContext<CommandSourceStack> context, ModifierType type) {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        CobbleBonusSavedData data = CobbleBonusSavedData.get(target.serverLevel());
        int count = data.clearModifiers(target.getUUID(), type);
        context.getSource().sendSuccess(
            () -> Component.literal("Cleared " + count + " " + type.getDisplayName() + " modifiers from " + target.getGameProfile().getName()),
            true
        );
        return count;
    }

    private static int showEffective(CommandContext<CommandSourceStack> context, ModifierType type) {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        CobbleBonusSavedData data = CobbleBonusSavedData.get(target.serverLevel());
        double cap = type == ModifierType.SHINY
            ? CobbleBonusConfig.MAX_EFFECTIVE_SHINY_MULTIPLIER.get()
            : CobbleBonusConfig.MAX_EFFECTIVE_CAPTURE_MULTIPLIER.get();
        double effective = data.getEffectiveMultiplier(target.getUUID(), type, cap);
        context.getSource().sendSuccess(
            () -> Component.literal(type.getDisplayName() + " effective multiplier for " + target.getGameProfile().getName() + ": x" + effective),
            false
        );
        return 1;
    }
}
