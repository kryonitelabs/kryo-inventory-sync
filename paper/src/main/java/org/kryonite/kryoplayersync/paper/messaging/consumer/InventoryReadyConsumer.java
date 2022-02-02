package org.kryonite.kryoplayersync.paper.messaging.consumer;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.kryonite.kryomessaging.service.message.Message;
import org.kryonite.kryomessaging.service.message.MessageCallback;
import org.kryonite.kryoplayersync.paper.messaging.message.InventoryReady;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;

@Slf4j
@RequiredArgsConstructor
public class InventoryReadyConsumer implements MessageCallback<InventoryReady> {

  private final Server server;
  private final String serverName;
  private final PlayerSyncManager playerSyncManager;

  @Override
  public void messageReceived(Message<InventoryReady> message) {
    if (serverName.equals(message.getBody().getSender())) {
      return;
    }

    UUID uniqueId = message.getBody().getUniqueId();
    Player player = server.getPlayer(uniqueId);
    if (player != null && player.isOnline()) {
      log.info("Was online already");
      playerSyncManager.syncInventory(player);
    } else {
      log.info("Add ready");
      playerSyncManager.addInventoryReady(uniqueId);
    }
  }
}
