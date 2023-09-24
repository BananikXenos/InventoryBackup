# InventoryBackup Minecraft Server Plugin

![Minecraft Logo](https://www.minecraft.net/etc.clientlibs/minecraft/clientlibs/main/resources/img/header/logo.png)

InventoryBackup is a Minecraft server plugin designed to backup and restore player inventories on death or on demand. This plugin allows server administrators to easily manage and recover player inventories, providing a more seamless and enjoyable gameplay experience.

## Features

- Automatic backup of player inventories on death.
- Manual backup and restore commands for player inventories.
- Purge old backup inventories to save disk space.
- Detailed timestamped records of player inventory backups.

## Installation

1. Download the latest release from the [Releases](https://github.com/BananikXenos/InventoryBackup/releases) page.
2. Place the downloaded JAR file in the `plugins` directory of your Minecraft server.
3. Restart or reload your server.

## Commands

### `/inventorybackup list [player]`

- List all backup inventories for the specified player.
- If no player is specified, it lists backups for the executing player.

### `/inventorybackup backup [player]`

- Create a backup of the specified player's inventory.
- If no player is specified, it backs up the inventory of the executing player.

### `/inventorybackup restore [player] [id]`

- Restore a player's inventory from a specific backup.
- If no player is specified, it restores the inventory of the executing player.
- The `[id]` parameter can be the backup ID or "latest" for the most recent backup.

### `/inventorybackup purge [player]`

- Purge all backup inventories of the specified player.
- If no player is specified, it purges the backups of the executing player.

### `/inventorybackup remove [player] [id]`

- Remove a specific backup inventory for the specified player.
- If no player is specified, it removes the backup for the executing player.
- The `[id]` parameter can be the backup ID or "latest" for the most recent backup.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you find InventoryBackup useful, please consider supporting the project by starring this repository and/or donating.

- [Making a donation via PayPal](https://paypal.me/scgxenos)
- [Buying me a coffee](https://www.buymeacoffee.com/synse)

Your support is greatly appreciated!

## Libraries

- [Gson](https://github.com/google/gson) - A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [Nitrite Java](https://github.com/nitrite/nitrite-java) - A performant embedded persistent NoSQL document store for Java.
- [CommandAPI](https://github.com/JorelAli/CommandAPI) - A simple and easy-to-use library to create commands for the Spigot API.

## Acknowledgments

- [Minecraft](https://www.minecraft.net/) - The game that inspired this plugin.
- [Spigot](https://www.spigotmc.org/) - The server software this plugin is designed for.
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) - The IDE used to develop this plugin.
- [Maven](https://maven.apache.org/) - The build automation tool used to build this plugin.

