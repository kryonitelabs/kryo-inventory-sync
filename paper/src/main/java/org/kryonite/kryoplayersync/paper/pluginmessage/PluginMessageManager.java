package org.kryonite.kryoplayersync.paper.pluginmessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.kryonite.kryoplayersync.common.PluginMessage;
import org.kryonite.kryoplayersync.paper.messaging.MessagingController;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;

@Slf4j
@RequiredArgsConstructor
public class PluginMessageManager {

  private final Plugin plugin;
  private final Server server;
  private final PlayerDataSyncManager playerDataSyncManager;
  private final MessagingController messagingController;

  public void setupPluginMessageListener() {
    Messenger messenger = server.getMessenger();

    String channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.SAVE_PLAYER_DATA.getValue();
    messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, message) ->
        preparePlayerDataForServerSwitch(player)
    );

    channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.INITIAL_JOIN.getValue();
    messenger.registerIncomingPluginChannel(plugin, channel, (channelName, player, message) ->
        playerDataSyncManager.addJoiningPlayer(player.getUniqueId()));
  }

  protected void preparePlayerDataForServerSwitch(Player player) {
    playerDataSyncManager.addSwitchingServers(player.getUniqueId());
    playerDataSyncManager.savePlayerData(player).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        log.error("Failed to save player data", throwable.getCause());
        return;
      }

      messagingController.sendPlayerDataReadyMessage(player.getUniqueId());
    });
  }
}
