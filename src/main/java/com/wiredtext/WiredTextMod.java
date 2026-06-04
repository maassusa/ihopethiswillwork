package com.wiredtext;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class WiredTextMod implements ModInitializer {

    public static boolean serverActive = false;
    public static boolean serverOverdrive = false;
    public static long serverMinMs = 60_000L;
    public static long serverMaxMs = 360_000L;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(WiredTextSyncPayload.ID, WiredTextSyncPayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("wt")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("on").executes(ctx -> {
                    serverActive = true;
                    serverOverdrive = false;
                    broadcast(ctx.getSource().getServer());
                    ctx.getSource().sendFeedback(() -> Text.literal("§7[WiredText] Включён."), true);
                    return 1;
                }))
                .then(CommandManager.literal("off").executes(ctx -> {
                    serverActive = false;
                    serverOverdrive = false;
                    broadcast(ctx.getSource().getServer());
                    ctx.getSource().sendFeedback(() -> Text.literal("§7[WiredText] Выключен."), true);
                    return 1;
                }))
                .then(CommandManager.literal("rate")
                    .then(CommandManager.argument("min", FloatArgumentType.floatArg(0.1f, 999f))
                        .then(CommandManager.argument("max", FloatArgumentType.floatArg(0.1f, 999f))
                            .executes(ctx -> {
                                float min = FloatArgumentType.getFloat(ctx, "min");
                                float max = FloatArgumentType.getFloat(ctx, "max");
                                if (min >= max) {
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cМин должен быть меньше макс!"), false);
                                    return 0;
                                }
                                serverMinMs = (long)(min * 1000f);
                                serverMaxMs = (long)(max * 1000f);
                                broadcast(ctx.getSource().getServer());
                                ctx.getSource().sendFeedback(() -> Text.literal("§7[WiredText] Интервал: " + min + "–" + max + " сек."), true);
                                return 1;
                            }))))
                .then(CommandManager.literal("overdrive")
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> {
                            serverOverdrive = BoolArgumentType.getBool(ctx, "enabled");
                            if (serverOverdrive) serverActive = true;
                            broadcast(ctx.getSource().getServer());
                            ctx.getSource().sendFeedback(() -> Text.literal("§7[WiredText] Overdrive: " + serverOverdrive), true);
                            return 1;
                        })))
            );
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            ServerPlayNetworking.send(handler.player,
                new WiredTextSyncPayload(serverActive, serverOverdrive, serverMinMs, serverMaxMs))
        );
    }

    public static void broadcast(net.minecraft.server.MinecraftServer server) {
        var pkt = new WiredTextSyncPayload(serverActive, serverOverdrive, serverMinMs, serverMaxMs);
        for (var player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, pkt);
        }
    }
}
