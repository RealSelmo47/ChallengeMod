package com.challengemods;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ModeManager {
    public static final Set<Mode> ACTIVE = EnumSet.noneOf(Mode.class);
    public static volatile long seed = 0L;

    /** Block -> Item (Block-Randomizer) */
    public static Map<Block, Item> dropMap = Map.of();
    /** Block -> Block (Welt-Remix) */
    public static Map<Block, Block> worldMap = Map.of();
    /** Zielbloecke fuer Welt-Remix und Chunk-Tausch */
    public static List<Block> targets = List.of();

    /** Diese Bloecke werden NIE veraendert (Portale + Rahmen). */
    public static final Set<Block> PROTECTED = Set.of(
            Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY,
            Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.BEDROCK);

    private static final Set<String> BANNED_ITEMS = Set.of(
            "air", "command_block", "chain_command_block", "repeating_command_block",
            "command_block_minecart", "structure_block", "structure_void", "jigsaw",
            "barrier", "light", "debug_stick", "knowledge_book");

    private static int autosave = 0;

    private ModeManager() {}

    private static Path file(MinecraftServer s) {
        return s.getSavePath(WorldSavePath.ROOT).resolve("challengemods.json");
    }

    private static Path chunkFile(MinecraftServer s) {
        return s.getSavePath(WorldSavePath.ROOT).resolve("challengemods_chunks.bin");
    }

    public static boolean isActive(Mode m) {
        return ACTIVE.contains(m);
    }

    public static void onServerStarting(MinecraftServer server) {
        ACTIVE.clear();
        Path f = file(server);
        try {
            if (Files.exists(f)) {
                JsonObject o = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
                seed = o.get("seed").getAsLong();
                for (JsonElement e : o.getAsJsonArray("modes")) {
                    try { ACTIVE.add(Mode.valueOf(e.getAsString())); } catch (IllegalArgumentException ignored) {}
                }
            } else if (PendingModes.armed) {
                seed = new Random().nextLong();
                ACTIVE.addAll(PendingModes.selected);
                JsonObject o = new JsonObject();
                o.addProperty("seed", seed);
                JsonArray arr = new JsonArray();
                ACTIVE.forEach(m -> arr.add(m.name()));
                o.add("modes", arr);
                Files.writeString(f, o.toString());
            }
        } catch (Exception e) {
            ChallengeMods.LOGGER.error("Konnte Challenge-Modi nicht laden/speichern", e);
        }
        PendingModes.armed = false;

        buildMaps();
        WorldRemix.load(chunkFile(server));
        RedLight.reset();
        ChunkSwap.reset();
        ChallengeMods.LOGGER.info("Aktive Challenge-Modi: {}", ACTIVE);
    }

    public static void onServerStopping(MinecraftServer server) {
        WorldRemix.save(chunkFile(server));
    }

    public static void onTick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;
        RedLight.tick(server);
        ChunkSwap.tick(server);
        if (++autosave >= 6000) {
            autosave = 0;
            WorldRemix.save(chunkFile(server));
        }
    }

    // ---------- Mapping-Aufbau ----------

    public static boolean isProtected(Block b) {
        return PROTECTED.contains(b);
    }

    private static boolean isSafeTarget(Block b) {
        BlockState s = b.getDefaultState();
        if (s.isAir() || !s.getFluidState().isEmpty() || s.hasBlockEntity()) return false;
        if (b instanceof FallingBlock || isProtected(b)) return false;
        String path = Registries.BLOCK.getId(b).getPath();
        if (path.equals("barrier") || path.equals("structure_void") || path.equals("light")) return false;
        return s.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
    }

    private static boolean isMappable(Block b) {
        BlockState s = b.getDefaultState();
        return !s.isAir() && !(b instanceof FluidBlock) && !(b instanceof BubbleColumnBlock)
                && !s.hasBlockEntity() && !isProtected(b);
    }

    private static void buildMaps() {
        List<Block> blocks = new ArrayList<>(Registries.BLOCK.stream().toList());
        blocks.sort(Comparator.comparing(b -> Registries.BLOCK.getId(b).toString()));

        // Block-Randomizer
        List<Item> items = new ArrayList<>();
        for (Item i : Registries.ITEM) {
            if (!BANNED_ITEMS.contains(Registries.ITEM.getId(i).getPath())) items.add(i);
        }
        items.sort(Comparator.comparing(i -> Registries.ITEM.getId(i).toString()));
        Collections.shuffle(items, new Random(seed));
        Map<Block, Item> dm = new HashMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            dm.put(blocks.get(i), items.get(i % items.size()));
        }
        dropMap = dm;

        // Welt-Remix + Chunk-Tausch
        List<Block> t = new ArrayList<>();
        for (Block b : blocks) if (isSafeTarget(b)) t.add(b);
        targets = t;
        List<Block> shuffled = new ArrayList<>(t);
        Collections.shuffle(shuffled, new Random(seed ^ 0x9E3779B97F4A7C15L));
        Map<Block, Block> wm = new HashMap<>();
        int idx = 0;
        for (Block b : blocks) {
            if (isMappable(b)) {
                wm.put(b, shuffled.get(idx % shuffled.size()));
                idx++;
            }
        }
        worldMap = wm;
    }
}
