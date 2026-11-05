package com.challengemods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChunkSwap {
    private static final int INTERVAL = 200; // 10 Sekunden
    private static int timer = INTERVAL;

    private ChunkSwap() {}

    public static void reset() {
        timer = INTERVAL;
    }

    public static void tick(MinecraftServer server) {
        if (!ModeManager.isActive(Mode.CHUNK_SWAP) || ModeManager.targets.isEmpty()) return;
        timer--;
        List<ServerPlayerEntity> players = List.copyOf(server.getPlayerManager().getPlayerList());
        if (timer <= 0) {
            timer = INTERVAL;
            Set<String> done = new HashSet<>();
            for (ServerPlayerEntity p : players) {
                if (p.isSpectator() || !p.isAlive()) continue;
                ServerWorld w = p.getServerWorld();
                ChunkPos cp = p.getChunkPos();
                if (done.add(w.getRegistryKey().getValue() + ":" + cp.toLong())) {
                    swap(w, cp, players);
                }
            }
        } else if (timer % 20 == 0) {
            for (ServerPlayerEntity p : players) {
                p.sendMessage(Text.literal("Chunk-Tausch in " + (timer / 20) + "s"), true);
            }
        }
    }

    private static void swap(ServerWorld world, ChunkPos cp, List<ServerPlayerEntity> players) {
        List<Block> t = ModeManager.targets;
        Block target = t.get(world.random.nextInt(t.size()));
        BlockState ns = target.getDefaultState();
        WorldChunk chunk = world.getChunk(cp.x, cp.z);

        // Positionen der Spieler (Fuesse + Kopf) bleiben unangetastet
        Set<Long> keep = new HashSet<>();
        for (ServerPlayerEntity p : world.getPlayers()) {
            keep.add(p.getBlockPos().asLong());
            keep.add(p.getBlockPos().up().asLong());
        }

        ChunkSection[] sections = chunk.getSectionArray();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection sec = sections[i];
            if (sec.isEmpty()) continue;
            int baseY = chunk.sectionIndexToCoord(i) << 4;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                BlockState old = sec.getBlockState(x, y, z);
                if (old.isAir() || old.getBlock() == target || ModeManager.isProtected(old.getBlock())) continue;
                pos.set(cp.getStartX() + x, baseY + y, cp.getStartZ() + z);
                if (keep.contains(pos.asLong())) continue;
                chunk.setBlockState(pos, ns, false);
            }
        }
        chunk.setNeedsSaving(true);

        int vd = world.getServer().getPlayerManager().getViewDistance();
        for (ServerPlayerEntity pl : world.getPlayers()) {
            ChunkPos pp = pl.getChunkPos();
            if (Math.max(Math.abs(pp.x - cp.x), Math.abs(pp.z - cp.z)) <= vd) {
                pl.networkHandler.sendPacket(new ChunkDataS2CPacket(chunk, world.getLightingProvider(), null, null));
            }
        }
        for (ServerPlayerEntity p : players) {
            if (p.getServerWorld() == world && p.getChunkPos().equals(cp)) {
                p.sendMessage(Text.literal("Dein Chunk ist jetzt: " + Registries.BLOCK.getId(target).getPath()), true);
            }
        }
    }
}
