package org.kryonite.kryoplayersync.paper.messaging;

import com.rabbitmq.client.BuiltinExchangeType;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.kryonite.kryomessaging.api.MessagingService;
import org.kryonite.kryomessaging.service.message.Message;
import org.kryonite.kryoplayersync.paper.messaging.consumer.InventoryReadyConsumer;
import org.kryonite.kryoplayersync.paper.messaging.message.InventoryReady;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;

@RequiredArgsConstructor
public class MessagingController {

  protected static final String INVENTORY_READY_EXCHANGE = "inventory_ready";

  private final MessagingService messagingService;
  private final Server server;
  private final PlayerSyncManager playerSyncManager;
  private final String serverName;

  public void setupInventoryReady() throws IOException {
    messagingService.setupExchange(INVENTORY_READY_EXCHANGE, BuiltinExchangeType.FANOUT);

    String queue = INVENTORY_READY_EXCHANGE + "_" + serverName;
    messagingService.bindQueueToExchange(queue, INVENTORY_READY_EXCHANGE);
    messagingService.startConsuming(
        queue,
        new InventoryReadyConsumer(server, serverName, playerSyncManager),
        InventoryReady.class
    );
  }

  public void sendInventoryReadyMessage(UUID uniqueId) {
    messagingService.sendMessage(Message.create(INVENTORY_READY_EXCHANGE, new InventoryReady(uniqueId, serverName)));
  }
}
