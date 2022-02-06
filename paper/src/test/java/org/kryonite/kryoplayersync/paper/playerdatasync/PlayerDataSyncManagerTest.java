package org.kryonite.kryoplayersync.paper.playerdatasync;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.awaitility.Awaitility;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerDataSyncManagerTest {

  @InjectMocks
  private PlayerDataSyncManager testee;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private InventorySyncManager inventorySyncManagerMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private EconomySyncManager economySyncManagerMock;

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
  void shouldLoadPlayerData_WhenPlayerDataWasReady() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    when(player.getUniqueId()).thenReturn(uniqueId);

    testee.addPlayerDataReady(uniqueId);

    // Act
    testee.syncIfReady(player);

    // Assert
    verify(inventorySyncManagerMock).loadInventory(player);
    verify(economySyncManagerMock).loadBalance(player);
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
  void shouldForceSyncPlayerData_WhenPlayerDataWasNotReadyButTimoutWasExceeded() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    when(player.getUniqueId()).thenReturn(uniqueId);
    when(serverMock.getPlayer(uniqueId)).thenReturn(player);
    when(player.isOnline()).thenReturn(true);

    // Act
    testee.syncIfReady(player);

    // Assert
    Awaitility.await()
        .pollDelay(2500, TimeUnit.MILLISECONDS)
        .atMost(5000, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> {
          verify(inventorySyncManagerMock).loadInventory(player);
          verify(economySyncManagerMock).loadBalance(player);
        });
  }

  @Test
  void shouldSavePlayerData() {
    // Arrange
    Player player = mock(Player.class);

    // Act
    testee.savePlayerData(player);

    // Assert
    verify(inventorySyncManagerMock).saveInventory(player);
    verify(economySyncManagerMock).saveBalance(player);
  }
}
