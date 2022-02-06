package org.kryonite.kryoplayersync.paper.playerdatasync;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import org.bukkit.entity.Player;

@Slf4j
public class PlayerDataSyncManager {

  private final Map<UUID, Long> joiningPlayers = new ConcurrentHashMap<>();
  private final Map<UUID, Long> playerDataReady = new ConcurrentHashMap<>();
  private final Map<UUID, Long> playerDataNotReady = new ConcurrentHashMap<>();
  private final Map<UUID, Long> switchingServers = new ConcurrentHashMap<>();

  private final InventorySyncManager inventorySyncManager;
  private final EconomySyncManager economySyncManager;
  private final Server server;

  public PlayerDataSyncManager(InventorySyncManager inventorySyncManager,
                               EconomySyncManager economySyncManager,
                               Server server) {
    this.inventorySyncManager = inventorySyncManager;
    this.economySyncManager = economySyncManager;
    this.server = server;

    setupExecutors();
  }

  private void setupExecutors() {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    executorService.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      joiningPlayers.values().removeIf(timestamp -> timestamp + 5000 < now);
      playerDataReady.values().removeIf(timestamp -> timestamp + 5000 < now);
      playerDataNotReady.values().removeIf(timestamp -> timestamp + 5000 < now);
      switchingServers.values().removeIf(timestamp -> timestamp + 500 < now);

      syncPlayerDataWhenMessageWasNotReceivedAfterTimeout();
    }, 500, 500, TimeUnit.MILLISECONDS);

    SaveAllPlayerData saveAllPlayerData = new SaveAllPlayerData(inventorySyncManager, economySyncManager);
    executorService.scheduleAtFixedRate(saveAllPlayerData, 60, 60, TimeUnit.SECONDS);
  }

  private void syncPlayerDataWhenMessageWasNotReceivedAfterTimeout() {
    long now = System.currentTimeMillis();
    Set<UUID> playersToSync = playerDataNotReady.entrySet().stream()
        .filter(entry -> entry.getValue() + 2500 < now)
        .filter(entry -> {
          Player player = server.getPlayer(entry.getKey());
          return player != null && player.isOnline();
        })
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
    playerDataNotReady.keySet().removeAll(playersToSync);
    playersToSync.forEach(uniqueId -> {
      Player player = server.getPlayer(uniqueId);
      if (player != null && player.isOnline()) {
        log.warn("PlayerData of " + uniqueId + " was synced after timeout!");
        loadPlayerData(player);
      }
    });
  }

  public void addJoiningPlayer(UUID uniqueId) {
    joiningPlayers.put(uniqueId, System.currentTimeMillis());
  }

  public boolean removeJoiningPlayer(UUID uniqueId) {
    return joiningPlayers.remove(uniqueId) != null;
  }

  public void addPlayerDataReady(UUID uniqueId) {
    playerDataReady.put(uniqueId, System.currentTimeMillis());
  }

  public void addSwitchingServers(UUID uniqueId) {
    switchingServers.put(uniqueId, System.currentTimeMillis());
  }

  public boolean isSwitchingServers(UUID uniqueId) {
    return switchingServers.containsKey(uniqueId);
  }

  public void syncIfReady(Player player) {
    UUID uniqueId = player.getUniqueId();
    if (playerDataReady.remove(uniqueId) != null) {
      playerDataNotReady.remove(uniqueId);
      loadPlayerData(player);
    } else {
      playerDataNotReady.put(uniqueId, System.currentTimeMillis());
    }
  }

  public void loadPlayerData(Player player) {
    playerDataReady.remove(player.getUniqueId());
    playerDataNotReady.remove(player.getUniqueId());

    inventorySyncManager.loadInventory(player);
    economySyncManager.loadBalance(player);
  }

  public CompletableFuture<Void> savePlayerData(Player player) {
    return CompletableFuture.allOf(inventorySyncManager.saveInventory(player), economySyncManager.saveBalance(player));
  }
}
