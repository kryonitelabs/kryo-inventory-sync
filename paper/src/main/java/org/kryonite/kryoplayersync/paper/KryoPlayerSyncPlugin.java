package org.kryonite.kryoplayersync.paper;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.kryonite.kryoplayersync.common.PluginMessage;
import org.kryonite.kryoplayersync.paper.listener.PlayerListener;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;

@Slf4j
public class KryoPlayerSyncPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    PlayerSyncManager playerSyncManager = new PlayerSyncManager();

    Messenger messenger = getServer().getMessenger();

    String channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.SAVE_PLAYER_DATA.getValue();
    messenger.registerIncomingPluginChannel(this, channel, (channel1, player, message) -> {
      log.info(player.getName());
      // TODO: save playerdata
      // TODO: send message
    });

    channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.INITIAL_JOIN.getValue();
    messenger.registerIncomingPluginChannel(this, channel, (channel1, player, message) -> {
      playerSyncManager.addJoiningPlayer(player.getUniqueId());
    });

    getServer().getPluginManager().registerEvents(new PlayerListener(playerSyncManager), this);
  }
}
