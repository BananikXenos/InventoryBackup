package com.synsenetwork.inventorybackup.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is used to serialize and deserialize ItemStack arrays.
 * It is used by Gson to convert ItemStack arrays to and from JSON.
 */
public class ItemStackArrayTypeAdapter extends TypeAdapter<ItemStack[]> {

    /**
     * This method is used to serialize an ItemStack array to JSON.
     * @param out The JSON writer
     * @param itemStackArray The ItemStack array to serialize
     * @throws IOException If an error occurs while writing to the JSON
     */
    @Override
    public void write(JsonWriter out, ItemStack[] itemStackArray) throws IOException {
        // If the ItemStack array is null, write a null value to the JSON
        if (itemStackArray == null) {
            out.nullValue();
            return;
        }

        // Create a new output stream and object output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        // Write the size of the array to the output stream
        dataOutput.writeInt(itemStackArray.length);

        // Write each item in the array to the output stream
        for (ItemStack itemStack : itemStackArray) {
            dataOutput.writeObject(itemStack);
        }

        // Close the object output stream
        dataOutput.close();

        // Encode the output stream to Base64
        String base64 = Base64Coder.encodeLines(outputStream.toByteArray());

        // Write the Base64 string to the JSON
        out.value(base64);
    }

    /**
     * This method is used to deserialize an ItemStack array from JSON.
     * @param in The JSON reader
     * @return The deserialized ItemStack array
     * @throws IOException If an error occurs while reading from the JSON
     */
    @Override
    public ItemStack[] read(JsonReader in) throws IOException {
        // Read the base64 encoded string from the JSON
        String base64 = in.nextString();

        // If the Base64 string is null or empty, return null
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try {
            // Create a new input stream and object input stream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Create a new ItemStack array
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read each item from the input stream
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            // Close the object input stream
            dataInput.close();

            // Return the ItemStack array
            return items;
        }catch (ClassNotFoundException e) {
            // If the ItemStack class cannot be found, throw an IOException
            throw new IOException("Unable to decode ItemStack array.", e);
        }
    }
}
