package com.challengemods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChallengeMods implements ModInitializer {
    public static final String MOD_ID = "challengemods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(LightPayload.ID, LightPayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTING.register(ModeManager::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(ModeManager::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(ModeManager::onTick);
        ServerChunkEvents.CHUNK_LOAD.register(WorldRemix::onChunkLoad);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                RedLight.sendTo(handler.getPlayer()));
    }
}
