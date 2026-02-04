package com.cobblebonus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CobbleBonusCommands {

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cobblebonus")
                .requires(source -> source.hasPermission(2))
                .then(buildModifierRoot("shiny", true))
                .then(buildModifierRoot("capture", false))
                .then(buildEffectiveRoot("shiny", true))
                .then(buildEffectiveRoot("capture", false))
        );
    }

    private static LiteralCommandNode<CommandSourceStack> buildModifierRoot(
        String root,
        boolean shiny
    ) {
        return Commands.literal(root)
            .then(
                Commands.literal("modifier")
                    .then(
                        Commands.literal("add")
                            .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("id", StringArgumentType.word())
                                    .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0D))
                                        .executes(context -> addModifier(context, shiny))
                                    )
                                )
                            )
                    )
                    .then(
                        Commands.literal("remove")
                            .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("id", StringArgumentType.word())
                                    .executes(context -> removeModifier(context, shiny))
                                )
                            )
                    )
                    .then(
                        Commands.literal("list")
                            .then(Commands.argument("target", EntityArgument.players())
                                .executes(context -> listModifiers(context, shiny))
                            )
                    )
                    .then(
                        Commands.literal("clear")
                            .then(Commands.argument("target", EntityArgument.players())
                                .executes(context -> clearModifiers(context, shiny))
                            )
                    )
            )
            .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildEffectiveRoot(
        String root,
        boolean shiny
    ) {
        return Commands.literal(root)
            .then(
                Commands.literal("effective")
                    .then(Commands.argument("target", EntityArgument.players())
                        .executes(context -> showEffective(context, shiny))
                    )
            )
            .build();
    }

    private static int addModifier(
        CommandContext<CommandSourceStack> context,
        boolean shiny
    ) throws CommandSyntaxException {
        double multiplier = DoubleArgumentType.getDouble(context, "multiplier");
        if (multiplier <= 0.0D) {
            context.getSource().sendFailure(Component.literal("Multiplier must be > 0."));
            return 0;
        }
        String id = StringArgumentType.getString(context, "id");
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
        for (ServerPlayer target : targets) {
            ModifierEntry entry = new ModifierEntry(id, multiplier);
            if (shiny) {
                ModifierManager.setShinyModifier(target, entry);
            } else {
                ModifierManager.setCaptureModifier(target, entry);
            }
        }
        context.getSource().sendSuccess(
            () -> Component.literal("Added modifier to " + targets.size() + " player(s)."),
            true
        );
        return targets.size();
    }

    private static int removeModifier(CommandContext<CommandSourceStack> context, boolean shiny)
        throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "id");
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
        int removedCount = 0;
        for (ServerPlayer target : targets) {
            boolean removed = shiny
                ? ModifierManager.removeShinyModifier(target, id)
                : ModifierManager.removeCaptureModifier(target, id);
            if (removed) {
                removedCount++;
            }
        }
        int finalRemovedCount = removedCount;
        context.getSource().sendSuccess(
            () -> Component.literal("Removed modifier from " + finalRemovedCount + " player(s)."),
            true
        );
        return removedCount;
    }

    private static int listModifiers(CommandContext<CommandSourceStack> context, boolean shiny)
        throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
        for (ServerPlayer target : targets) {
            PlayerModifierData data = ModifierManager.getPlayerData(target);
            context.getSource().sendSuccess(
                () -> Component.literal(
                    "Modifiers for " + target.getGameProfile().getName() + " (" + (shiny ? "shiny" : "capture") + "):"
                ),
                false
            );
            if (shiny) {
                sendModifierList(context, data.getShinyModifiers());
            } else {
                sendModifierList(context, data.getCaptureModifiers());
            }
        }
        return targets.size();
    }

    private static void sendModifierList(
        CommandContext<CommandSourceStack> context,
        java.util.Map<String, ModifierEntry> modifiers
    ) {
        if (modifiers.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal(" - (none)"), false);
            return;
        }
        for (ModifierEntry entry : modifiers.values()) {
            context.getSource().sendSuccess(
                () -> Component.literal(" - " + entry.getId() + " x" + entry.getMultiplier()),
                false
            );
        }
    }

    private static int clearModifiers(CommandContext<CommandSourceStack> context, boolean shiny)
        throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
        for (ServerPlayer target : targets) {
            if (shiny) {
                ModifierManager.clearShiny(target);
            } else {
                ModifierManager.clearCapture(target);
            }
        }
        context.getSource().sendSuccess(
            () -> Component.literal("Cleared modifiers for " + targets.size() + " player(s)."),
            true
        );
        return targets.size();
    }

    private static int showEffective(CommandContext<CommandSourceStack> context, boolean shiny)
        throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
        for (ServerPlayer target : targets) {
            PlayerModifierData data = ModifierManager.getPlayerData(target);
            double cap = shiny
                ? CobbleBonusConfig.MAX_EFFECTIVE_SHINY_MULTIPLIER.get()
                : CobbleBonusConfig.MAX_EFFECTIVE_CAPTURE_MULTIPLIER.get();
            double raw = shiny ? data.getRawShinyMultiplier() : data.getRawCaptureMultiplier();
            double value = Math.min(raw, cap);
            String message = "Effective " + (shiny ? "shiny" : "capture") + " multiplier for "
                + target.getGameProfile().getName() + ": x" + value
                + " (raw x" + raw + ", cap x" + cap + ")";
            context.getSource().sendSuccess(
                () -> Component.literal(message),
                false
            );
        }
        return targets.size();
    }
}
