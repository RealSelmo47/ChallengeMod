package com.challengemods.client;

import com.challengemods.LightPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class ChallengeModsClient implements ClientModInitializer {
    private static volatile int phase = -1;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(LightPayload.ID, (payload, ctx) -> phase = payload.phase());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> phase = -1);
        HudRenderCallback.EVENT.register(ChallengeModsClient::renderHud);
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tick) {
        int p = phase;
        if (p < 0) return;

        int size = 16, gap = 4, pad = 4;
        int total = 3 * size + 2 * gap + 2 * pad;
        int x0 = ctx.getScaledWindowWidth() / 2 - total / 2;
        int y0 = 6;
        ctx.fill(x0, y0, x0 + total, y0 + size + 2 * pad, 0xCC000000);

        // links Rot, Mitte Orange, rechts Gruen
        int[] bright = {0xFFFF2828, 0xFFFFA020, 0xFF28E040};
        int[] dim = {0xFF3A1010, 0xFF3A2808, 0xFF0E3A14};
        int[] phaseOfSlot = {2, 1, 0};
        for (int i = 0; i < 3; i++) {
            int x = x0 + pad + i * (size + gap);
            int color = (p == phaseOfSlot[i]) ? bright[i] : dim[i];
            ctx.fill(x, y0 + pad, x + size, y0 + pad + size, color);
        }
    }
}
