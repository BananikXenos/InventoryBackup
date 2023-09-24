package com.synsenetwork.inventorybackup.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.synsenetwork.inventorybackup.data.ItemSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Base64;

public class ItemStackArrayTypeAdapter extends TypeAdapter<ItemStack[]> {

    @Override
    public void write(JsonWriter out, ItemStack[] itemStackArray) throws IOException {
        if (itemStackArray == null) {
            out.nullValue();
            return;
        }

        // Convert the ItemStack array to a Base64 string and write it to the JSON
        String base64 = ItemSerialization.itemStackArrayToBase64(itemStackArray);
        out.value(base64);
    }

    @Override
    public ItemStack[] read(JsonReader in) throws IOException {
        String base64 = in.nextString();

        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        // Convert the Base64 string back to an ItemStack array
        return ItemSerialization.itemStackArrayFromBase64(base64);
    }
}
