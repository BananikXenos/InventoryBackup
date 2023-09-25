package com.synsenetwork.inventorybackup.listeners;

import com.synsenetwork.inventorybackup.data.BackupInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.logging.Logger;

/**
 * A listener for events related to the creation of backup inventories.
 */
public class EventListener implements Listener {
    // Logger
    private final Logger logger = Logger.getLogger(EventListener.class.getName());
    private final ObjectRepository<BackupInventory> repository;

    public EventListener(ObjectRepository<BackupInventory> repository) {
        this.repository = repository;
    }

    /**
     * Creates a backup inventory when a player dies.
     *
     * @param event The event.
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Get player
        Player player = event.getPlayer();

        // Create backup inventory
        BackupInventory backupInventory = BackupInventory.create(player);

        // Insert backup inventory into database
        repository.insert(backupInventory);

        // Log backup inventory creation
        logger.info("Created backup inventory on death for player " + player.getName() + " with id " + backupInventory.getIdField().getIdValue().longValue() + ".");
    }
}
