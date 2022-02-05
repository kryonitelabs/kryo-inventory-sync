package org.kryonite.kryoplayersync.paper.pluginmessage;

import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.kryonite.kryoplayersync.common.PluginMessage;
import org.kryonite.kryoplayersync.paper.messaging.MessagingController;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;

@RequiredArgsConstructor
public class PluginMessageManager {

  private final Plugin plugin;
  private final Server server;
  private final PlayerDataSyncManager playerDataSyncManager;
  private final MessagingController messagingController;

  public void setupPluginMessageListener() {
    Messenger messenger = server.getMessenger();

    String channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.SAVE_PLAYER_DATA.getValue();
    messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, message) -> {
      playerDataSyncManager.savePlayerData(player);
      messagingController.sendPlayerDataReadyMessage(player.getUniqueId());
      playerDataSyncManager.addSwitchingServers(player.getUniqueId());
    });

    channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.INITIAL_JOIN.getValue();
    messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, message) ->
        playerDataSyncManager.addJoiningPlayer(player.getUniqueId()));
  }
}
