package org.kryonite.kryoplayersync.paper.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;

@Slf4j
@RequiredArgsConstructor
public class PlayerListener implements Listener {

  private final PlayerDataSyncManager playerDataSyncManager;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (playerDataSyncManager.removeJoiningPlayer(player.getUniqueId())) {
      playerDataSyncManager.loadPlayerData(player);
    } else {
      playerDataSyncManager.syncIfReady(player);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (!playerDataSyncManager.isSwitchingServers(player.getUniqueId())) {
      playerDataSyncManager.savePlayerData(player).whenComplete((unused, throwable) -> {
        if (throwable != null) {
          log.error("Failed to save player data", throwable.getCause());
        }
      });
    }
  }
}
