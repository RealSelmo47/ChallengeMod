package com.challengemods;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WorldRemix {
    /** Bereits umgewandelte Chunks pro Dimension (damit nichts doppelt gemappt wird). */
    private static final Map<Identifier, LongSet> DONE = new HashMap<>();

    private WorldRemix() {}

    public static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        if (!ModeManager.isActive(Mode.WORLD_REMIX)) return;
        Identifier dim = world.getRegistryKey().getValue();
        LongSet done = DONE.computeIfAbsent(dim, k -> new LongOpenHashSet());
        if (!done.add(chunk.getPos().toLong())) return;
        remix(chunk);
    }

    private static void remix(WorldChunk chunk) {
        Map<Block, Block> map = ModeManager.worldMap;
        ChunkPos cp = chunk.getPos();
        ChunkSection[] sections = chunk.getSectionArray();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection sec = sections[i];
            if (sec.isEmpty() || !sec.hasAny(s -> map.containsKey(s.getBlock()))) continue;
            int baseY = chunk.sectionIndexToCoord(i) << 4;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                BlockState old = sec.getBlockState(x, y, z);
                Block nb = map.get(old.getBlock());
                if (nb == null || old.hasBlockEntity()) continue;
                pos.set(cp.getStartX() + x, baseY + y, cp.getStartZ() + z);
                chunk.setBlockState(pos, nb.getDefaultState(), false);
            }
        }
    }

    public static void load(Path f) {
        DONE.clear();
        if (!Files.exists(f)) return;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(f)))) {
            int n = in.readInt();
            for (int i = 0; i < n; i++) {
                String key = in.readUTF();
                int size = in.readInt();
                LongSet set = new LongOpenHashSet(size);
                for (int j = 0; j < size; j++) set.add(in.readLong());
                Identifier id = Identifier.tryParse(key);
                if (id != null) DONE.put(id, set);
            }
        } catch (IOException e) {
            ChallengeMods.LOGGER.error("Konnte Chunk-Liste nicht laden", e);
        }
    }

    public static void save(Path f) {
        if (DONE.isEmpty()) return;
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(f)))) {
            out.writeInt(DONE.size());
            for (Map.Entry<Identifier, LongSet> e : DONE.entrySet()) {
                out.writeUTF(e.getKey().toString());
                out.writeInt(e.getValue().size());
                for (long l : e.getValue()) out.writeLong(l);
            }
        } catch (IOException e) {
            ChallengeMods.LOGGER.error("Konnte Chunk-Liste nicht speichern", e);
        }
    }
}
