package org.kryonite.kryoplayersync.paper.messaging;

import static org.kryonite.kryoplayersync.paper.messaging.MessagingController.INVENTORY_READY_EXCHANGE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.BuiltinExchangeType;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryomessaging.api.MessagingService;
import org.kryonite.kryomessaging.service.message.Message;
import org.kryonite.kryoplayersync.paper.messaging.consumer.InventoryReadyConsumer;
import org.kryonite.kryoplayersync.paper.messaging.message.InventoryReady;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessagingControllerTest {

  private final String serverName = "testee";

  private MessagingController testee;

  @Mock
  private MessagingService messagingServiceMock;

  @Mock
  private Server serverMock;

  @Mock
  private PlayerSyncManager playerSyncManagerMock;

  @BeforeEach
  void setup() {
    testee = new MessagingController(messagingServiceMock, serverMock, playerSyncManagerMock, serverName);
  }

  @Test
  void shouldSetupInventoryReady() throws IOException {
    // Arrange - Act
    testee.setupInventoryReady();

    // Assert
    verify(messagingServiceMock).setupExchange(INVENTORY_READY_EXCHANGE, BuiltinExchangeType.FANOUT);

    String queue = INVENTORY_READY_EXCHANGE + "_" + serverName;
    verify(messagingServiceMock).bindQueueToExchange(queue, INVENTORY_READY_EXCHANGE);
    verify(messagingServiceMock).startConsuming(eq(queue), any(InventoryReadyConsumer.class), eq(InventoryReady.class));
  }

  @Test
  void shouldSendInventoryReadyMessage() {
    // Arrange
    UUID uniqueId = UUID.randomUUID();

    // Act
    testee.sendInventoryReadyMessage(uniqueId);

    // Assert
    verify(messagingServiceMock).sendMessage(
        Message.create(INVENTORY_READY_EXCHANGE, new InventoryReady(uniqueId, serverName))
    );
  }
}
