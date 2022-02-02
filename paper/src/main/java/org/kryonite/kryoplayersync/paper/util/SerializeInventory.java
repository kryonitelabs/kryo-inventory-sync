package org.kryonite.kryoplayersync.paper.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class SerializeInventory {

  public static byte[] toByteArray(PlayerInventory playerInventory) throws IOException {
    return toByteArray(playerInventory.getContents());
  }

  public static byte[] toByteArray(ItemStack[] items) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

    dataOutput.writeInt(items.length);

    for (ItemStack itemStack : items) {
      if (itemStack != null) {
        dataOutput.writeObject(itemStack.serializeAsBytes());
      } else {
        dataOutput.writeObject(null);
      }
    }

    dataOutput.close();
    return outputStream.toByteArray();
  }

  public static ItemStack[] toItemStackArray(byte[] data) throws IOException {
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      ItemStack[] items = new ItemStack[dataInput.readInt()];

      for (int i = 0; i < items.length; i++) {
        byte[] stack = (byte[]) dataInput.readObject();

        if (stack != null) {
          items[i] = ItemStack.deserializeBytes(stack);
        } else {
          items[i] = null;
        }
      }

      dataInput.close();
      return items;
    } catch (ClassNotFoundException e) {
      throw new IOException("Unable to decode class type.", e);
    }
  }
}
