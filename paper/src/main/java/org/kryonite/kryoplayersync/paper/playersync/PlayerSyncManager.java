package org.kryonite.kryoplayersync.paper.playersync;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.util.SerializeInventory;

@Slf4j
public class PlayerSyncManager {

  private final Map<UUID, Long> joiningPlayers = new ConcurrentHashMap<>();
  private final Map<UUID, Long> inventoryReady = new ConcurrentHashMap<>();

  private final InventoryRepository inventoryRepository;

  public PlayerSyncManager(InventoryRepository inventoryRepository) {
    this.inventoryRepository = inventoryRepository;

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      joiningPlayers.values().removeIf(timestamp -> timestamp + 5000 < now);
      inventoryReady.values().removeIf(timestamp -> timestamp + 5000 < now);
    }, 1, 1, TimeUnit.SECONDS);
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

  public void syncIfReady(Player player) {
    if (inventoryReady.remove(player.getUniqueId()) != null) {
      log.info("Was ready");
      syncInventory(player);
    } else {
      log.info("Inv was not ready");
      // TODO: sync after 5 seconds if message not received
    }
  }

  public void syncInventory(Player player) {
    inventoryReady.remove(player.getUniqueId());

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

  // save inventory every 60 seconds
}
