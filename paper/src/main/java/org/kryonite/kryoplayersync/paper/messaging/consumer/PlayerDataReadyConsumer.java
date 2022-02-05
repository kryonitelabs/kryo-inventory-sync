package org.kryonite.kryoplayersync.paper.messaging.consumer;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.kryonite.kryomessaging.service.message.Message;
import org.kryonite.kryomessaging.service.message.MessageCallback;
import org.kryonite.kryoplayersync.paper.messaging.message.PlayerDataReady;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;

@RequiredArgsConstructor
public class PlayerDataReadyConsumer implements MessageCallback<PlayerDataReady> {

  private final Server server;
  private final String serverName;
  private final PlayerDataSyncManager playerDataSyncManager;

  @Override
  public void messageReceived(Message<PlayerDataReady> message) {
    if (serverName.equals(message.getBody().getSender())) {
      return;
    }

    UUID uniqueId = message.getBody().getUniqueId();
    Player player = server.getPlayer(uniqueId);
    if (player != null && player.isOnline()) {
      playerDataSyncManager.loadPlayerData(player);
    } else {
      playerDataSyncManager.addPlayerDataReady(uniqueId);
    }
  }
}
