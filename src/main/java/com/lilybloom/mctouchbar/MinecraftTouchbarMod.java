package com.lilybloom.mctouchbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("mctouchbar")
public class MinecraftTouchbarMod {
    private final SocketServer socketServer = new SocketServer();

    public MinecraftTouchbarMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLClientSetupEvent event) {
        socketServer.start();
        // Add shutdown hook to stop the server when the game exits
        Runtime.getRuntime().addShutdownHook(new Thread(socketServer::stop));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (player != null && level != null) {
            // Coordinates
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            // Biome retrieval
            String biome = level.registryAccess()
                    .registryOrThrow(Registries.BIOME)
                    .getKey(level.getBiome(player.blockPosition()).value())
                    .toString();

            // Weather detection
            String weather = "Clear";
            if (level.isThundering()) {
                weather = "Thunder";
            } else if (level.isRaining()) {
                weather = "Rain";
            }

            // 24h World Time calculation
            long worldTime = level.getDayTime();
            long timeOfDay = worldTime % 24000;
            int totalMinutes = (int) (((timeOfDay + 6000) % 24000) * (1440.0 / 24000.0));
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            String timeStr = String.format("%02d:%02d", hours, minutes);
            long days = worldTime / 24000;

            socketServer.broadcast(new SocketServer.GameData(x, y, z, biome, weather, timeStr, worldTime, days));
        }
    }
}
