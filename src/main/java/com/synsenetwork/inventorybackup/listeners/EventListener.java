package com.synsenetwork.inventorybackup.listeners;

import com.synsenetwork.inventorybackup.data.BackupInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.dizitart.no2.objects.ObjectRepository;

public class EventListener implements Listener {
    private final ObjectRepository<BackupInventory> repository;

    public EventListener(ObjectRepository<BackupInventory> repository) {
        this.repository = repository;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Get player
        Player player = event.getPlayer();

        // Create backup inventory
        BackupInventory backupInventory = BackupInventory.create(player);

        // Insert backup inventory into database
        repository.insert(backupInventory);
    }
}
