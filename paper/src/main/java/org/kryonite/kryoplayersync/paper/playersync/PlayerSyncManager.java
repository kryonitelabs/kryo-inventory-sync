package org.kryonite.kryoplayersync.paper.playersync;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.util.SerializeInventory;

@Slf4j
public class PlayerSyncManager {

  private final Map<UUID, Long> joiningPlayers = new ConcurrentHashMap<>();
  private final Map<UUID, Long> inventoryReady = new ConcurrentHashMap<>();
  private final Map<UUID, Long> inventoryNotReady = new ConcurrentHashMap<>();
  private final Map<UUID, Long> switchingServers = new ConcurrentHashMap<>();

  private final InventoryRepository inventoryRepository;
  private final Server server;

  public PlayerSyncManager(InventoryRepository inventoryRepository, Server server) {
    this.inventoryRepository = inventoryRepository;
    this.server = server;

    setupExecutors();
  }

  private void setupExecutors() {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    executorService.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      joiningPlayers.values().removeIf(timestamp -> timestamp + 5000 < now);
      inventoryReady.values().removeIf(timestamp -> timestamp + 5000 < now);
      inventoryNotReady.values().removeIf(timestamp -> timestamp + 5000 < now);
      switchingServers.values().removeIf(timestamp -> timestamp + 500 < now);

      syncInventoryWhenMessageWasNotReceivedAfterTimeout();
    }, 500, 500, TimeUnit.MILLISECONDS);

    executorService.scheduleAtFixedRate(() -> server.getOnlinePlayers().forEach(this::syncInventory),
        // TODO: change to multi save
        60, 60, TimeUnit.SECONDS);
  }

  private void syncInventoryWhenMessageWasNotReceivedAfterTimeout() {
    long now = System.currentTimeMillis();
    Set<UUID> playersToSync = inventoryNotReady.entrySet().stream()
        .filter(entry -> entry.getValue() + 2500 < now)
        .filter(entry -> {
          Player player = server.getPlayer(entry.getKey());
          return player != null && player.isOnline();
        })
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
    inventoryNotReady.keySet().removeAll(playersToSync);
    playersToSync.forEach(uniqueId -> {
      Player player = server.getPlayer(uniqueId);
      if (player != null && player.isOnline()) {
        log.warn("Inventory of " + uniqueId + " was synced after timeout!");
        syncInventory(player);
      }
    });
  }

  public void addJoiningPlayer(UUID uniqueId) {
    joiningPlayers.put(uniqueId, System.currentTimeMillis());
  }

  public boolean removeJoiningPlayer(UUID uniqueId) {
    return joiningPlayers.remove(uniqueId) != null;
  }

  public void addInventoryReady(UUID uniqueId) {
    inventoryReady.put(uniqueId, System.currentTimeMillis());
  }

  public void addSwitchingServers(UUID uniqueId) {
    switchingServers.put(uniqueId, System.currentTimeMillis());
  }

  public boolean isSwitchingServers(UUID uniqueId) {
    return switchingServers.containsKey(uniqueId);
  }

  public void syncIfReady(Player player) {
    UUID uniqueId = player.getUniqueId();
    if (inventoryReady.remove(uniqueId) != null) {
      inventoryNotReady.remove(uniqueId);
      syncInventory(player);
    } else {
      inventoryNotReady.put(uniqueId, System.currentTimeMillis());
    }
  }

  public void syncInventory(Player player) {
    inventoryReady.remove(player.getUniqueId());
    inventoryNotReady.remove(player.getUniqueId());

    try {
      Optional<byte[]> inventory = inventoryRepository.get(player.getUniqueId());
      if (inventory.isPresent()) {
        ItemStack[] itemStacks = SerializeInventory.toItemStackArray(inventory.get());
        player.getInventory().setContents(itemStacks);
      }
    } catch (SQLException | IOException exception) {
      log.error("Failed to load inventory", exception);
    }
  }

  public void saveInventory(Player player) {
    try {
      byte[] inventory = SerializeInventory.toByteArray(player.getInventory());
      inventoryRepository.save(player.getUniqueId(), inventory);
    } catch (IOException | SQLException exception) {
      log.error("Failed to save inventory", exception);
    }
  }
}
