package org.kryonite.kryoplayersync.paper.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

class SerializeInventoryTest {

  @Test
  void shouldSerializeInventory() throws IOException {
    // Arrange
    PlayerInventory playerInventory = mock(PlayerInventory.class);
    ItemStack itemStack = mock(ItemStack.class);
    byte[] bytes = {};
    ItemStack[] contents = new ItemStack[] {
        itemStack,
        null
    };

    when(playerInventory.getContents()).thenReturn(contents);
    when(itemStack.serializeAsBytes()).thenReturn(bytes);

    // Act
    byte[] inventory = SerializeInventory.toByteArray(playerInventory);

    // Assert
    assertNotNull(inventory);
  }
}
