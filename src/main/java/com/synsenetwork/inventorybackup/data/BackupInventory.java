package com.synsenetwork.inventorybackup.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synsenetwork.inventorybackup.gson.ItemStackArrayTypeAdapter;
import com.synsenetwork.inventorybackup.gson.ItemStackTypeAdapter;
import com.synsenetwork.inventorybackup.utils.Experience;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.UUID;

/**
 * Represents a backup of a player's inventory.
 */
@Indices({
        @Index(value = "playerId", type = IndexType.NonUnique),
        @Index(value = "timestamp", type = IndexType.Unique)
})
public class BackupInventory implements Mappable {
    @Id
    private NitriteId idField;
    private UUID playerId;
    private long timestamp;
    private ItemStack[] armorContents;
    private ItemStack[] extraContents;
    private ItemStack[] contents;
    private int totalExperience;

    // Gson instance
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .registerTypeAdapter(ItemStack[].class, new ItemStackArrayTypeAdapter())
            .create();

    /**
     * Creates a new BackupInventory object.
     *
     * @param playerId        The UUID of the player.
     * @param timestamp       The timestamp of when the inventory was backed up.
     * @param armorContents   The armor contents of the player.
     * @param extraContents   The extra contents of the player.
     * @param contents        The contents of the player.
     * @param totalExperience The total experience of the player.
     */
    private BackupInventory(UUID playerId, long timestamp, ItemStack[] armorContents, ItemStack[] extraContents, ItemStack[] contents, int totalExperience) {
        this.playerId = playerId;
        this.timestamp = timestamp;
        this.armorContents = armorContents;
        this.extraContents = extraContents;
        this.contents = contents;
        this.totalExperience = totalExperience;
    }


    /**
     * Creates a new BackupInventory object from a player's inventory.
     *
     * @param player The player to create the BackupInventory object from.
     * @return A new BackupInventory object.
     */
    public static BackupInventory create(Player player) {
        return new BackupInventory(
                player.getUniqueId(),
                System.currentTimeMillis(),
                player.getInventory().getArmorContents(),
                player.getInventory().getExtraContents(),
                player.getInventory().getContents(),
                Experience.getExp(player));
    }

    /**
     * Restores the inventory to the player.
     *
     * @param player The player to restore the inventory to.
     */
    public void restore(Player player) {
        player.getInventory().setArmorContents(armorContents);
        player.getInventory().setExtraContents(extraContents);
        player.getInventory().setContents(contents);
        Experience.changeExp(player, Experience.getExp(player));
    }


    /**
     * Gets the armor contents of the player.
     *
     * @return The armor contents of the player.
     */
    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    /**
     * Gets the extra contents of the player.
     *
     * @return The extra contents of the player.
     */
    public ItemStack[] getExtraContents() {
        return extraContents;
    }

    /**
     * Gets the contents of the player.
     *
     * @return The contents of the player.
     */
    public ItemStack[] getContents() {
        return contents;
    }

    /**
     * Gets the UUID of the player.
     *
     * @return The UUID of the player.
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the total experience of the player.
     *
     * @return The total experience of the player.
     */
    public int getTotalExperience() {
        return totalExperience;
    }

    /**
     * Gets the timestamp of when the inventory was backed up.
     *
     * @return The timestamp of when the inventory was backed up.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the id field of the object.
     *
     * @return The id field of the object.
     */
    public NitriteId getIdField() {
        return idField;
    }

    /**
     * Writes the object to a document.
     *
     * @param mapper The NitriteMapper instance.
     * @return The document to write the object to.
     */
    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("playerId", playerId.toString());
        document.put("timestamp", timestamp);
        document.put("armorContents", gson.toJson(armorContents));
        document.put("extraContents", gson.toJson(extraContents));
        document.put("contents", gson.toJson(contents));
        document.put("totalExperience", totalExperience);
        return document;
    }

    /**
     * Reads the object from a document.
     *
     * @param mapper   The NitriteMapper instance.
     * @param document The document to read the object from.
     */
    @Override
    public void read(NitriteMapper mapper, Document document) {
        this.idField = NitriteId.createId(document.get("idField", Long.class));
        this.playerId = UUID.fromString(document.get("playerId", String.class));
        this.timestamp = document.get("timestamp", Long.class);
        this.armorContents = gson.fromJson(document.get("armorContents", String.class), ItemStack[].class);
        this.extraContents = gson.fromJson(document.get("extraContents", String.class), ItemStack[].class);
        this.contents = gson.fromJson(document.get("contents", String.class), ItemStack[].class);
        this.totalExperience = document.get("totalExperience", Integer.class);
    }
}
