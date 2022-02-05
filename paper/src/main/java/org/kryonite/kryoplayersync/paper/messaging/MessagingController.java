package org.kryonite.kryoplayersync.paper.messaging;

import com.rabbitmq.client.BuiltinExchangeType;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.kryonite.kryomessaging.api.MessagingService;
import org.kryonite.kryomessaging.service.message.Message;
import org.kryonite.kryoplayersync.paper.messaging.consumer.PlayerDataReadyConsumer;
import org.kryonite.kryoplayersync.paper.messaging.message.PlayerDataReady;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;

@RequiredArgsConstructor
public class MessagingController {

  protected static final String PLAYER_DATA_READY_EXCHANGE = "player_data_ready";

  private final MessagingService messagingService;
  private final Server server;
  private final PlayerDataSyncManager playerDataSyncManager;
  private final String serverName;

  public void setupPlayerDataReady() throws IOException {
    messagingService.setupExchange(PLAYER_DATA_READY_EXCHANGE, BuiltinExchangeType.FANOUT);

    String queue = PLAYER_DATA_READY_EXCHANGE + "_" + serverName;
    messagingService.bindQueueToExchange(queue, PLAYER_DATA_READY_EXCHANGE);
    messagingService.startConsuming(
        queue,
        new PlayerDataReadyConsumer(server, serverName, playerDataSyncManager),
        PlayerDataReady.class
    );
  }

  public void sendPlayerDataReadyMessage(UUID uniqueId) {
    messagingService.sendMessage(Message.create(PLAYER_DATA_READY_EXCHANGE, new PlayerDataReady(uniqueId, serverName)));
  }
}
