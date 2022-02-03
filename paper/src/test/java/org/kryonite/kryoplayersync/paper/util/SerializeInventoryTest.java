package org.kryonite.kryoplayersync.paper.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SerializeInventoryTest {

  @Test
  void shouldSerializeAndDeserializeInventory() throws IOException {
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

    try (MockedStatic<ItemStack> itemStackMockedStatic = mockStatic(ItemStack.class)) {
      itemStackMockedStatic
          .when(() -> ItemStack.deserializeBytes(bytes))
          .thenReturn(itemStack);

      // Act
      byte[] inventory = SerializeInventory.toByteArray(playerInventory);
      ItemStack[] result = SerializeInventory.toItemStackArray(inventory);

      // Assert
      assertNotNull(inventory);
      assertNotNull(result);
      assertArrayEquals(contents, result);
    }
  }
}
