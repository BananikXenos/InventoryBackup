package com.synsenetwork.inventorybackup;

import com.synsenetwork.inventorybackup.data.BackupInventory;
import com.synsenetwork.inventorybackup.listeners.EventListener;
import com.synsenetwork.inventorybackup.utils.TimeUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * InventoryBackup main class
 */
public final class InventoryBackup extends JavaPlugin {
    // Logger
    private static final Logger LOGGER = Logger.getLogger(InventoryBackup.class.getName());

    // Database
    private Nitrite db;
    private ObjectRepository<BackupInventory> repository;

    @Override
    public void onLoad() {
        // Log command api loading
        LOGGER.info("Loading CommandAPI...");

        // Register commands api
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        // Log command api enabling
        LOGGER.info("Enabling CommandAPI...");

        // Register commands api
        CommandAPI.onEnable();

        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            // Log data folder creation
            LOGGER.info("Creating data folder at " + getDataFolder().getAbsolutePath() + "...");

            // Create data folder
            getDataFolder().mkdir();
        }

        // Get database file
        File databaseFile = new File(getDataFolder(), "inventory.db");

        // Log database loading
        LOGGER.info("Loading database at " + databaseFile.getAbsolutePath() + "...");

        // Create database
        db = Nitrite.builder().compressed().filePath(databaseFile.getAbsolutePath()).openOrCreate();

        // Log repository loading
        LOGGER.info("Loading repository...");

        // Create repository
        repository = db.getRepository(BackupInventory.class);

        // Log event listener registration
        LOGGER.info("Registering event listener...");

        // Register event listener
        getServer().getPluginManager().registerEvents(new EventListener(repository), this);

        // Log command registration
        LOGGER.info("Registering commands...");

        // Register commands
        new CommandAPICommand("inventorybackup").withAliases("invbackup", "backupinventory").withSubcommand(new CommandAPICommand("list").withOptionalArguments(new PlayerArgument("player")).executesPlayer((player, args) -> {
            // Get player
            Player target = args.get("player") == null ? player : (Player) args.get("player");

            // Load player's backup inventories
            Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));

            List<BackupInventory> backupList = new ArrayList<>();
            playerBackups.forEach(backupList::add);

            if (backupList.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No backup inventories found for " + target.getName() + ".");
                return;
            }

            // Send player's backup inventories
            player.sendMessage(ChatColor.GOLD + "Backup inventories of " + target.getName() + ":");
            for (BackupInventory backupInventory : backupList) {
                player.sendMessage(ChatColor.GOLD + " - ID: " + backupInventory.getIdField().getIdValue().longValue() + " | Time: " + TimeUtils.formatTime(backupInventory.getTimestamp()));
            }
        })).withSubcommand(new CommandAPICommand("backup").withOptionalArguments(new PlayerArgument("player")).executesPlayer((player, args) -> {
            // Get player
            Player target = args.get("player") == null ? player : (Player) args.get("player");

            // Create backup inventory
            BackupInventory backupInventory = BackupInventory.create(target);

            // Add backup inventory to player's backup inventories
            repository.insert(backupInventory);

            // Log backup inventory creation
            LOGGER.info("Created backup inventory on demand for " + target.getName() + ". ID: " + backupInventory.getIdField().getIdValue().longValue());

            // Send message
            player.sendMessage(ChatColor.GOLD + "Successfully backed up " + target.getName() + "'s inventory. ID: " + backupInventory.getIdField());
        })).withSubcommand(new CommandAPICommand("restore").withOptionalArguments(new PlayerArgument("player")).withOptionalArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync(info -> {
            return CompletableFuture.supplyAsync(() -> {
                List<String> suggestions = new ArrayList<>();
                suggestions.add("latest");

                // Get player
                Player target = info.previousArgs().get("player") == null ? info.sender() instanceof Player ? (Player) info.sender() : null : (Player) info.previousArgs().get("player");

                if (target == null) {
                    return suggestions;
                }

                // Load player's backup inventories
                Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));

                for (BackupInventory backupInventory : playerBackups) {
                    suggestions.add(String.valueOf(backupInventory.getIdField().getIdValue().longValue()));
                }

                return suggestions;
            });
        }))).executesPlayer((player, args) -> {
            // Get player
            Player target = args.get("player") == null ? player : (Player) args.get("player");
            String id = args.get("id") == null ? "latest" : (String) args.get("id");

            // Get backup inventory
            BackupInventory backupInventory = getBackupInventory(target, id);

            // Check if backup inventory exists
            if (backupInventory == null) {
                player.sendMessage(ChatColor.RED + "No backup inventories found for " + target.getName() + ".");
                return;
            }

            // Restore backup inventory
            backupInventory.restore(target);

            // Log backup inventory restoration
            LOGGER.info("Restored backup inventory for " + target.getName() + ". ID: " + backupInventory.getIdField().getIdValue().longValue());

            // Send message
            player.sendMessage(ChatColor.GOLD + "Successfully restored " + target.getName() + "'s inventory. ID: " + backupInventory.getTimestamp());
        })).withSubcommand(new CommandAPICommand("purge").withOptionalArguments(new PlayerArgument("player")).executesPlayer((player, args) -> {
            // Get player
            Player target = args.get("player") == null ? player : (Player) args.get("player");

            // Load player's backup inventories
            Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));

            // Delete player's backup inventories
            for (BackupInventory backupInventory : playerBackups) {
                repository.remove(backupInventory);
            }

            // Log backup inventory purging
            LOGGER.info("Purged backup inventories for " + target.getName() + ".");

            // Send message
            player.sendMessage(ChatColor.GOLD + "Successfully purged " + target.getName() + "'s backup inventories.");
        })).withSubcommand(new CommandAPICommand("remove").withOptionalArguments(new PlayerArgument("player")).withOptionalArguments(new StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync(info -> {
            return CompletableFuture.supplyAsync(() -> {
                List<String> suggestions = new ArrayList<>();
                suggestions.add("latest");

                // Get player
                Player target = info.previousArgs().get("player") == null ? info.sender() instanceof Player ? (Player) info.sender() : null : (Player) info.previousArgs().get("player");

                if (target == null) {
                    return suggestions;
                }

                // Load player's backup inventories
                Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));

                for (BackupInventory backupInventory : playerBackups) {
                    suggestions.add(String.valueOf(backupInventory.getIdField().getIdValue().longValue()));
                }

                return suggestions;
            });
        }))).executesPlayer((player, args) -> {
            // Get player
            Player target = args.get("player") == null ? player : (Player) args.get("player");
            String id = args.get("id") == null ? "latest" : (String) args.get("id");

            // Get backup inventory
            BackupInventory backupInventory = getBackupInventory(target, id);

            // Check if backup inventory exists
            if (backupInventory == null) {
                player.sendMessage(ChatColor.RED + "No backup inventories found for " + target.getName() + ".");
                return;
            }

            // Delete backup inventory
            repository.remove(backupInventory);

            // Log backup inventory deletion
            LOGGER.info("Deleted backup inventory for " + target.getName() + ". ID: " + backupInventory.getIdField().getIdValue().longValue());

            // Send message
            player.sendMessage(ChatColor.GOLD + "Successfully removed " + target.getName() + "'s inventory. ID: " + backupInventory.getTimestamp());
        })).register();
    }

    @Override
    public void onDisable() {
        // Log command api disabling
        LOGGER.info("Disabling CommandAPI...");

        // Unregister commands api
        CommandAPI.onDisable();

        // Log event listener unregistration
        LOGGER.info("Unregistering event listener...");

        // Unregister event listener
        HandlerList.unregisterAll(this);

        // Log database closing
        LOGGER.info("Closing database...");

        // Close database
        db.close();
    }

    /**
     * Get backup inventory by ID
     *
     * @param target Target player
     * @param id     Backup inventory ID
     * @return Backup inventory
     */
    private BackupInventory getBackupInventory(Player target, String id) {
        BackupInventory backupInventory = null;

        if (id.equals("latest")) {
            // Load player's backup inventories
            Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));
            for (BackupInventory currentBackupInventory : playerBackups) {
                if (backupInventory == null || currentBackupInventory.getTimestamp() > backupInventory.getTimestamp()) {
                    backupInventory = currentBackupInventory;
                }
            }
        } else {
            // Get backup inventory by ID
            NitriteId nitriteId = NitriteId.createId(Long.parseLong(id));
            backupInventory = repository.getById(nitriteId);
        }

        return backupInventory;
    }
}
