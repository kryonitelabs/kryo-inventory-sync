package org.kryonite.kryoplayersync.velocity.listener;

import static org.kryonite.kryoplayersync.common.PluginMessage.INITIAL_JOIN;
import static org.kryonite.kryoplayersync.common.PluginMessage.NAMESPACE;
import static org.kryonite.kryoplayersync.common.PluginMessage.SAVE_PLAYER_DATA;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerListener {

  @Subscribe(order = PostOrder.LAST)
  public void onServerPreConnect(ServerPreConnectEvent event) {
    sendPluginMessage(
        event.getPlayer(),
        MinecraftChannelIdentifier.create(NAMESPACE.getValue(), SAVE_PLAYER_DATA.getValue())
    );
  }

  @Subscribe
  public void onServerConnected(ServerPostConnectEvent event) {
    log.info("initial: " + event.getPreviousServer());

    if (event.getPreviousServer() == null) {
      sendPluginMessage(
          event.getPlayer(),
          MinecraftChannelIdentifier.create(NAMESPACE.getValue(), INITIAL_JOIN.getValue())
      );
    }
  }

  // TODO: save inventory on disconnect

  private void sendPluginMessage(Player player, MinecraftChannelIdentifier minecraftChannelIdentifier) {
    if (player.getCurrentServer().isPresent()) {
      ServerConnection currentServer = player.getCurrentServer().get();
      currentServer.sendPluginMessage(minecraftChannelIdentifier, new byte[] {});
    }
  }
}
