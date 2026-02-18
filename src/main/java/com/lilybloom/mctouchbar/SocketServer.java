package com.lilybloom.mctouchbar;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static final int PORT = 25005;
    private static final Logger LOGGER = LogManager.getLogger();
    private final CopyOnWriteArrayList<Socket> clients = new CopyOnWriteArrayList<>();
    private final ExecutorService broadcastExecutor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private boolean running = true;
    private ServerSocket serverSocket;

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                LOGGER.info("Minecraft Touchbar Socket Server started on port " + PORT);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    clients.add(clientSocket);
                    LOGGER.info("Client connected: " + clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                if (running) {
                    LOGGER.error("Socket server error", e);
                }
            }
        }, "MCTouchbar-SocketServer").start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        
        for (Socket client : clients) {
            try {
                client.close();
            } catch (IOException ignored) {}
        }
        clients.clear();
        broadcastExecutor.shutdownNow();
        LOGGER.info("Socket server stopped.");
    }

    public void broadcast(GameData data) {
        if (clients.isEmpty()) return;

        // Broadcast asynchronously to avoid blocking the main game thread
        broadcastExecutor.submit(() -> {
            String json = gson.toJson(data) + "\n";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            for (Socket client : clients) {
                try {
                    if (client.isClosed() || !client.isConnected()) {
                        clients.remove(client);
                        continue;
                    }
                    client.getOutputStream().write(bytes);
                    client.getOutputStream().flush();
                } catch (IOException e) {
                    try {
                        client.close();
                    } catch (IOException ignored) {}
                    clients.remove(client);
                }
            }
        });
    }

    public static class GameData {
        public double x, y, z;
        public String biome;
        public String weather;
        public String time;
        public long worldTime;
        public long days;

        public GameData(double x, double y, double z, String biome, String weather, String time, long worldTime, long days) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.biome = biome;
            this.weather = weather;
            this.time = time;
            this.worldTime = worldTime;
            this.days = days;
        }
    }
}
