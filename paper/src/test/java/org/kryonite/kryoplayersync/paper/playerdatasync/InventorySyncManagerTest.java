package org.kryonite.kryoplayersync.paper.playerdatasync;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.util.SerializeInventory;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventorySyncManagerTest {

  @InjectMocks
  private InventorySyncManager testee;

  @Mock
  private InventoryRepository inventoryRepositoryMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Server serverMock;

  @Test
  void shouldSaveInventory() throws SQLException {
    // Arrange
    Player player = mock(Player.class);
    UUID uniqueId = UUID.randomUUID();
    PlayerInventory playerInventory = mock(PlayerInventory.class);
    byte[] inventory = new byte[] {11, 12};

    when(player.getUniqueId()).thenReturn(uniqueId);
    when(player.getInventory()).thenReturn(playerInventory);

    try (MockedStatic<SerializeInventory> serializeInventoryMockedStatic = mockStatic(SerializeInventory.class)) {
      serializeInventoryMockedStatic
          .when(() -> SerializeInventory.toByteArray(playerInventory))
          .thenReturn(inventory);

      // Act
      testee.saveInventory(player);

      // Assert
      verify(inventoryRepositoryMock).save(uniqueId, inventory);
    }
  }

  @Test
  void shouldLoadInventory() throws SQLException {
    // Arrange
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    UUID uniqueId = UUID.randomUUID();
    byte[] inventory = new byte[] {11, 12};

    ItemStack[] itemStacks = new ItemStack[] {
        null,
        mock(ItemStack.class)
    };

    when(player.getUniqueId()).thenReturn(uniqueId);
    when(inventoryRepositoryMock.get(uniqueId)).thenReturn(Optional.of(inventory));

    try (MockedStatic<SerializeInventory> serializeInventoryMockedStatic = mockStatic(SerializeInventory.class)) {
      serializeInventoryMockedStatic
          .when(() -> SerializeInventory.toItemStackArray(inventory))
          .thenReturn(itemStacks);

      // Act
      testee.loadInventory(player);

      // Assert
      verify(player.getInventory()).setContents(itemStacks);
    }
  }

  @Test
  void shouldSyncAllRepositories() throws SQLException {
    // Arrange
    UUID uniqueId1 = UUID.randomUUID();
    UUID uniqueId2 = UUID.randomUUID();

    Player player1 = mock(Player.class);
    Player player2 = mock(Player.class);
    when(player1.getUniqueId()).thenReturn(uniqueId1);
    when(player2.getUniqueId()).thenReturn(uniqueId2);

    PlayerInventory playerInventory1 = mock(PlayerInventory.class);
    PlayerInventory playerInventory2 = mock(PlayerInventory.class);
    when(player1.getInventory()).thenReturn(playerInventory1);
    when(player2.getInventory()).thenReturn(playerInventory2);

    byte[] inventory1 = new byte[] {11, 12};
    byte[] inventory2 = new byte[] {13, 14};

    doReturn(Set.of(player1, player2)).when(serverMock).getOnlinePlayers();

    try (MockedStatic<SerializeInventory> serializeInventoryMockedStatic = mockStatic(SerializeInventory.class)) {
      serializeInventoryMockedStatic
          .when(() -> SerializeInventory.toByteArray(playerInventory1))
          .thenReturn(inventory1);
      serializeInventoryMockedStatic
          .when(() -> SerializeInventory.toByteArray(playerInventory2))
          .thenReturn(inventory2);

      // Act
      testee.saveAllInventories();

      // Assert
      verify(inventoryRepositoryMock).saveAll(Map.of(uniqueId1, inventory1, uniqueId2, inventory2));
    }
  }
}
