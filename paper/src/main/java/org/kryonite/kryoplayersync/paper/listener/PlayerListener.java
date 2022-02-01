package org.kryonite.kryoplayersync.paper.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    log.info("was present: " + playerSyncManager.removeJoiningPlayer(event.getPlayer().getUniqueId()));
  }
}
