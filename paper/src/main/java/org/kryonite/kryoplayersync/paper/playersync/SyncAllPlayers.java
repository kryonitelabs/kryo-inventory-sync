package org.kryonite.kryoplayersync.paper.playersync;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.util.SerializeInventory;

@Slf4j
@RequiredArgsConstructor
class SyncAllPlayers implements Runnable {

  private final InventoryRepository inventoryRepository;
  private final Server server;

  @Override
  public void run() {
    try {
      Map<UUID, Optional<byte[]>> inventories = collectInventories();
      inventoryRepository.saveAll(getPresentInventories(inventories));
    } catch (Exception exception) {
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
