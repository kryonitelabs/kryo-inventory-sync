package org.kryonite.kryoplayersync.paper.playerdatasync;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.util.SerializeInventory;

@Slf4j
@RequiredArgsConstructor
public class InventorySyncManager {

  private final InventoryRepository inventoryRepository;
  private final Server server;

  public void saveInventory(Player player) {
    try {
      byte[] inventory = SerializeInventory.toByteArray(player.getInventory());
      inventoryRepository.save(player.getUniqueId(), inventory);
    } catch (IOException | SQLException exception) {
      log.error("Failed to save inventory", exception);
    }
  }

  public void loadInventory(Player player) {
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

  public void saveAllInventories() {
    try {
      Map<UUID, Optional<byte[]>> inventories = collectInventories();
      inventoryRepository.saveAll(getPresentInventories(inventories));
    } catch (SQLException exception) {
      log.error("Failed to save all inventories", exception);
    }
  }

  private Map<UUID, Optional<byte[]>> collectInventories() {
    return server.getOnlinePlayers().stream()
        .collect(Collectors.toMap(
            Entity::getUniqueId,
            player -> {
              try {
                return Optional.of(SerializeInventory.toByteArray(player.getInventory()));
              } catch (IOException exception) {
                log.error("Failed to serialize inventory for {}", player.getUniqueId(), exception);
                return Optional.empty();
              }
            }
        ));
  }

  @NotNull
  private Map<UUID, byte[]> getPresentInventories(Map<UUID, Optional<byte[]>> inventories) {
    return inventories.entrySet().stream()
        .filter(entry -> entry.getValue().isPresent())
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
  }
}
