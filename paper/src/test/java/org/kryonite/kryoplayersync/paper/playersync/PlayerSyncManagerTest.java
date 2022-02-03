package org.kryonite.kryoplayersync.paper.playersync;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.awaitility.Awaitility;
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
class PlayerSyncManagerTest {

  @InjectMocks
  private PlayerSyncManager testee;

  @Mock
  private InventoryRepository inventoryRepositoryMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Server serverMock;

  @Test
  void shouldRemoveJoiningPlayerEntry_WhenFiveSecondsHavePassed() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();

    // Act
    testee.addJoiningPlayer(uniqueId);

    // Assert
    Awaitility.await()
        .pollDelay(6, TimeUnit.SECONDS)
        .atMost(7, TimeUnit.SECONDS)
        .until(() -> testee.removeJoiningPlayer(uniqueId), Predicate.isEqual(false));
  }

  @Test
  void shouldSyncInventory_WhenInventoryWasReady() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    when(player.getUniqueId()).thenReturn(uniqueId);

    byte[] inventory = new byte[] {};
    ItemStack[] itemStacks = {};
    try (MockedStatic<SerializeInventory> serializeInventoryMockedStatic = mockStatic(SerializeInventory.class)) {
      serializeInventoryMockedStatic
          .when(() -> SerializeInventory.toItemStackArray(inventory))
          .thenReturn(itemStacks);

      when(inventoryRepositoryMock.get(uniqueId)).thenReturn(Optional.of(inventory));

      testee.addInventoryReady(uniqueId);

      // Act
      testee.syncInventory(player);

      // Assert
      verify(inventoryRepositoryMock).get(uniqueId);
      verify(player.getInventory()).setContents(itemStacks);
    }
  }

  @Test
  void shouldNotSyncInventory_WhenFiveSecondsHavePassed() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);

    testee.addInventoryReady(uniqueId);

    // Act
    testee.syncInventory(player);

    // Assert
    Awaitility.await()
        .pollDelay(6, TimeUnit.SECONDS)
        .atMost(7, TimeUnit.SECONDS)
        .untilAsserted(() -> verify(inventoryRepositoryMock, never()).get(uniqueId));
  }

  @Test
  void shouldRemoveSwitchingServersEntry_WhenFiveSecondsHavePassed() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();

    // Act
    testee.addSwitchingServers(uniqueId);

    // Assert
    Awaitility.await()
        .pollDelay(6, TimeUnit.SECONDS)
        .atMost(7, TimeUnit.SECONDS)
        .until(() -> testee.isSwitchingServers(uniqueId), Predicate.isEqual(false));
  }

  @Test
  void shouldForceSyncInventory_WhenInventoryWasNotReadyButTimoutWasExceeded() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    when(player.getUniqueId()).thenReturn(uniqueId);
    when(player.isOnline()).thenReturn(true);
    when(serverMock.getPlayer(uniqueId)).thenReturn(player);

    when(inventoryRepositoryMock.get(uniqueId)).thenReturn(Optional.of(new byte[] {}));

    // Act
    testee.syncIfReady(player);

    // Assert
    Awaitility.await()
        .pollDelay(2500, TimeUnit.MILLISECONDS)
        .atMost(5000, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
          verify(inventoryRepositoryMock).get(uniqueId);
        });
  }

  @Test
  void shouldSaveInventory() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Player player = mock(Player.class);
    PlayerInventory playerInventory = mock(PlayerInventory.class);
    ItemStack itemStack = mock(ItemStack.class);

    when(player.getInventory()).thenReturn(playerInventory);
    when(player.getUniqueId()).thenReturn(uniqueId);
    when(playerInventory.getContents()).thenReturn(new ItemStack[] {
        itemStack,
        null
    });
    when(itemStack.serializeAsBytes()).thenReturn(new byte[] {});

    // Act
    testee.saveInventory(player);

    // Assert
    verify(inventoryRepositoryMock).save(eq(uniqueId), any());
  }
}
