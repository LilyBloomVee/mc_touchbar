# Minecraft World Stats (on Macbook Touchbar) Mod

A Minecraft Forge mod that exports real-time game data via a socket server to be displayed on a MacBook Touch Bar.

## Features

- **Real-time Data Export:** Broadcasts game information to connected clients over a local socket.
- **Game Information:**
    - Player Coordinates (X, Y, Z)
    - Current Biome
    - Weather Status (Clear, Rain, Thunder)
    - In-game Time (24h format and total days)
- **Socket Server:** Runs on port `8081` by default.

## Requirements

To see the output on a MacBook with a Touch Bar, you **must** have the following project running:

👉 **[mc_touchbar_bridge](https://github.com/LilyBloomVee/mc_touchbar_bridge)**

This bridge acts as the client that receives data from this mod and renders it onto the Touch Bar.

## Installation

1. Ensure you have Minecraft Forge installed.
2. Drop the mod JAR file into your Minecraft `mods` folder.
3. Start Minecraft.
4. Run the [mc_touchbar_bridge](https://github.com/LilyBloomVee/mc_touchbar_bridge) on your Mac.

## Technical Details

The mod starts a background socket server on port `8081`. Every client tick, it collects the player's current status and broadcasts it as a JSON object to all connected clients.

### Sample Data Format

```json
{
  "x": 123,
  "y": 64,
  "z": -789,
  "biome": "minecraft:plains",
  "weather": "Clear",
  "time": "12:00",
  "worldTime": 6000,
  "days": 0
}
```

## Development

Built with:
- Java 17
- Minecraft Forge
- Gradle
