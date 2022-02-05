package org.kryonite.kryoplayersync.paper.messaging.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryomessaging.service.message.Message;
import org.kryonite.kryoplayersync.paper.messaging.message.PlayerDataReady;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerDataReadyConsumerTest {

  private final String serverName = "testee";

  private PlayerDataReadyConsumer testee;

  @Mock
  private Server serverMock;

  @Mock
  private PlayerDataSyncManager playerDataSyncManagerMock;

  @BeforeEach
  void setup() {
    testee = new PlayerDataReadyConsumer(serverMock, serverName, playerDataSyncManagerMock);
  }

  @Test
  void shouldSyncInventory_WhenPlayerIsAlreadyPresent() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    String sender = "another-server";
    Message<PlayerDataReady> message = Message.create("test", new PlayerDataReady(uniqueId, sender));

    Player player = mock(Player.class);
    when(player.isOnline()).thenReturn(true);
    when(serverMock.getPlayer(uniqueId)).thenReturn(player);

    // Act
    testee.messageReceived(message);

    // Assert
    verify(playerDataSyncManagerMock).loadPlayerData(player);
  }

  @Test
  void shouldAddInventoryReady_WhenPlayerIsNotYetPresent() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    String sender = "another-server";
    Message<PlayerDataReady> message = Message.create("test", new PlayerDataReady(uniqueId, sender));

    Player player = mock(Player.class);
    when(player.isOnline()).thenReturn(false);
    when(serverMock.getPlayer(uniqueId)).thenReturn(player);

    // Act
    testee.messageReceived(message);

    // Assert
    verify(playerDataSyncManagerMock).addPlayerDataReady(uniqueId);
  }

  @Test
  void shouldDoNothing_WhenSenderIsCurrentServer() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Message<PlayerDataReady> message = Message.create("test", new PlayerDataReady(uniqueId, serverName));

    // Act
    testee.messageReceived(message);

    // Assert
    verify(playerDataSyncManagerMock, never()).loadPlayerData(any());
    verify(playerDataSyncManagerMock, never()).addPlayerDataReady(uniqueId);
  }
}
