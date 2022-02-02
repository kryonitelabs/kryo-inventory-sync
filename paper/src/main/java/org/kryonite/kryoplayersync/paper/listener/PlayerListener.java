package org.kryonite.kryoplayersync.paper.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;

@Slf4j
@RequiredArgsConstructor
public class PlayerListener implements Listener {

  private final PlayerSyncManager playerSyncManager;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (playerSyncManager.removeJoiningPlayer(player.getUniqueId())) {
      playerSyncManager.syncInventory(player);
    } else {
      playerSyncManager.syncIfReady(player);
    }
  }
}
