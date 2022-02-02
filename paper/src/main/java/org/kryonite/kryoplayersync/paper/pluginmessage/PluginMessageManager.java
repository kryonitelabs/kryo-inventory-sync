package org.kryonite.kryoplayersync.paper.pluginmessage;

import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.kryonite.kryoplayersync.common.PluginMessage;
import org.kryonite.kryoplayersync.paper.messaging.MessagingController;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;

@RequiredArgsConstructor
public class PluginMessageManager {

  private final Plugin plugin;
  private final Server server;
  private final PlayerSyncManager playerSyncManager;
  private final MessagingController messagingController;

  public void setupPluginMessageListener() {
    Messenger messenger = server.getMessenger();

    String channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.SAVE_PLAYER_DATA.getValue();
    messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, message) -> {
      playerSyncManager.saveInventory(player);
      messagingController.sendInventoryReadyMessage(player.getUniqueId());
      playerSyncManager.addSwitchingServers(player.getUniqueId());
    });

    channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.INITIAL_JOIN.getValue();
    messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, message) ->
        playerSyncManager.addJoiningPlayer(player.getUniqueId()));
  }
}
