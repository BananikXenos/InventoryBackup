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

public final class InventoryBackup extends JavaPlugin {
    // Database
    private Nitrite db;
    private ObjectRepository<BackupInventory> repository;

    @Override
    public void onLoad() {
        // Register commands api
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true));
    }

    @Override
    public void onEnable() {
        // Register commands api
        CommandAPI.onEnable();

        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Create database
        db = Nitrite.builder().compressed().filePath(new File(getDataFolder(), "inventory.db").getAbsolutePath()).openOrCreate();

        // Create repository
        repository = db.getRepository(BackupInventory.class);

        // Register event listener
        getServer().getPluginManager().registerEvents(new EventListener(repository), this);

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
            BackupInventory backupInventory = null;

            // Get backup inventory
            if (id.equals("latest")) {
                // Load player's backup inventories
                Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));
                for (BackupInventory currentBackupInventory : playerBackups) {
                    if (backupInventory == null || currentBackupInventory.getTimestamp() > backupInventory.getTimestamp()) {
                        backupInventory = currentBackupInventory;
                    }
                }
            } else {
                // Get backup inventory
                NitriteId nitriteId = NitriteId.createId(Long.parseLong(id));
                backupInventory = repository.getById(nitriteId);
            }

            // Check if backup inventory exists
            if (backupInventory == null) {
                player.sendMessage(ChatColor.RED + "No backup inventories found for " + target.getName() + ".");
                return;
            }

            // Restore backup inventory
            backupInventory.restore(target);

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
            BackupInventory backupInventory = null;

            // Get backup inventory
            if (id.equals("latest")) {
                // Load player's backup inventories
                Iterable<BackupInventory> playerBackups = repository.find(ObjectFilters.eq("playerId", target.getUniqueId()));
                for (BackupInventory currentBackupInventory : playerBackups) {
                    if (backupInventory == null || currentBackupInventory.getTimestamp() > backupInventory.getTimestamp()) {
                        backupInventory = currentBackupInventory;
                    }
                }
            } else {
                // Get backup inventory
                NitriteId nitriteId = NitriteId.createId(Long.parseLong(id));
                backupInventory = repository.getById(nitriteId);
            }

            // Check if backup inventory exists
            if (backupInventory == null) {
                player.sendMessage(ChatColor.RED + "No backup inventories found for " + target.getName() + ".");
                return;
            }

            // Delete backup inventory
            repository.remove(backupInventory);

            // Send message
            player.sendMessage(ChatColor.GOLD + "Successfully removed " + target.getName() + "'s inventory. ID: " + backupInventory.getTimestamp());
        })).register();
    }

    @Override
    public void onDisable() {
        // Unregister commands api
        CommandAPI.onDisable();

        // Unregister event listener
        HandlerList.unregisterAll(this);

        // Close database
        db.close();
    }
}
