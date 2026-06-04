package com.wiredtext;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class WiredTextClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(WiredTextSyncPayload.ID, (payload, context) ->
            context.client().execute(() -> {
                boolean wasActive = WiredTextState.active;
                WiredTextState.active = payload.active();
                WiredTextState.overdrive = payload.overdrive();
                WiredTextState.minDelayMs = payload.minDelayMs();
                WiredTextState.maxDelayMs = payload.maxDelayMs();
                if (payload.active() && !wasActive) WiredTextState.scheduleNext();
                if (!payload.active()) { WiredTextState.nextTriggerTime = 0; WiredTextState.showUntil = 0; }
            })
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> WiredTextState.tick());

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!WiredTextState.isShowing()) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return;

            var tr = client.textRenderer;
            int sw = client.getWindow().getScaledWidth();
            int sh = client.getWindow().getScaledHeight();
            String text = WiredTextState.currentText;
            int tw = tr.getWidth(text);
            int x = Math.max(2, Math.min((int)(WiredTextState.textX * sw) - tw / 2, sw - tw - 2));
            int y = Math.max(2, Math.min((int)(WiredTextState.textY * sh), sh - 10));
            int color = (55 << 24) | 0xAAAAAA;
            drawContext.drawText(tr, text, x, y, color, false);
        });
    }
}
