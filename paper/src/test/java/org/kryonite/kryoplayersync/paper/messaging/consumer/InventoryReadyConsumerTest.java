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
import org.kryonite.kryoplayersync.paper.messaging.message.InventoryReady;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryReadyConsumerTest {

  private final String serverName = "testee";

  private InventoryReadyConsumer testee;

  @Mock
  private Server serverMock;

  @Mock
  private PlayerSyncManager playerSyncManagerMock;

  @BeforeEach
  void setup() {
    testee = new InventoryReadyConsumer(serverMock, serverName, playerSyncManagerMock);
  }

  @Test
  void shouldSyncInventory_WhenPlayerIsAlreadyPresent() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    String sender = "another-server";
    Message<InventoryReady> message = Message.create("test", new InventoryReady(uniqueId, sender));

    Player player = mock(Player.class);
    when(player.isOnline()).thenReturn(true);
    when(serverMock.getPlayer(uniqueId)).thenReturn(player);

    // Act
    testee.messageReceived(message);

    // Assert
    verify(playerSyncManagerMock).syncInventory(player);
  }

  @Test
  void shouldAddInventoryReady_WhenPlayerIsNotYetPresent() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    String sender = "another-server";
    Message<InventoryReady> message = Message.create("test", new InventoryReady(uniqueId, sender));

    Player player = mock(Player.class);
    when(player.isOnline()).thenReturn(false);
    when(serverMock.getPlayer(uniqueId)).thenReturn(player);

    // Act
    testee.messageReceived(message);

    // Assert
    verify(playerSyncManagerMock).addInventoryReady(uniqueId);
  }

  @Test
  void shouldDoNothing_WhenSenderIsCurrentServer() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    Message<InventoryReady> message = Message.create("test", new InventoryReady(uniqueId, serverName));

    // Act
    testee.messageReceived(message);

    // Assert
    verify(playerSyncManagerMock, never()).syncInventory(any());
    verify(playerSyncManagerMock, never()).addInventoryReady(uniqueId);
  }
}
