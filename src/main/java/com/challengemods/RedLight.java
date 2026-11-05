package com.challengemods;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class RedLight {
    // 0 gruen, 1 orange, 2 rot
    private static int phase = 0;
    private static int ticksLeft = 100;
    private static int redTicks = 0;
    private static final int GRACE_TICKS = 10;       // Reaktionszeit nach Rot
    private static final Map<UUID, Vec3d> last = new HashMap<>();
    private static final Random RND = new Random();

    private RedLight() {}

    public static void reset() {
        phase = 0;
        ticksLeft = 60 + RND.nextInt(101);
        redTicks = 0;
        last.clear();
    }

    public static void sendTo(ServerPlayerEntity p) {
        int v = ModeManager.isActive(Mode.RED_LIGHT) ? phase : -1;
        ServerPlayNetworking.send(p, new LightPayload(v));
    }

    private static void broadcast(MinecraftServer s) {
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) sendTo(p);
    }

    public static void tick(MinecraftServer server) {
        if (!ModeManager.isActive(Mode.RED_LIGHT)) return;

        if (--ticksLeft <= 0) {
            switch (phase) {
                case 0 -> { phase = 1; ticksLeft = 30; }                       // Orange: 1,5 s
                case 1 -> { phase = 2; ticksLeft = 40 + RND.nextInt(61); redTicks = 0; } // Rot: 2-5 s
                default -> { phase = 0; ticksLeft = 60 + RND.nextInt(101); }   // Gruen: 3-8 s
            }
            broadcast(server);
        }
        if (phase == 2) redTicks++;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            Vec3d now = p.getPos();
            Vec3d prev = last.put(p.getUuid(), now);
            if (phase != 2 || redTicks <= GRACE_TICKS || prev == null) continue;
            if (!p.isAlive() || p.isSpectator() || p.isCreative()) continue;

            double dx = now.x - prev.x, dz = now.z - prev.z, dy = now.y - prev.y;
            double h2 = dx * dx + dz * dz;
            if (h2 > 25) continue;                 // Teleport/Respawn ignorieren
            if (h2 > 0.0004 || dy > 0.05) {        // gelaufen oder gesprungen
                server.getPlayerManager().broadcast(
                        Text.literal(p.getName().getString() + " hat sich bei ROT bewegt!"), false);
                p.damage(p.getDamageSources().genericKill(), Float.MAX_VALUE);
            }
        }
    }
}
