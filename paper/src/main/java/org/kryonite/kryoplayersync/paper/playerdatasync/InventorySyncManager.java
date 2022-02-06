package org.kryonite.kryoplayersync.paper.playerdatasync;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.util.SerializeInventory;

@Slf4j
@RequiredArgsConstructor
public class InventorySyncManager {

  private final InventoryRepository inventoryRepository;
  private final Server server;
  private final Plugin plugin;

  public CompletableFuture<Void> saveInventory(Player player) {
    try {
      byte[] inventory = SerializeInventory.toByteArray(player.getInventory());
      return inventoryRepository.save(player.getUniqueId(), inventory);
    } catch (IOException exception) {
      log.error("Failed to save inventory", exception);
      return CompletableFuture.failedFuture(exception);
    }
  }

  public void loadInventory(Player player) {
    inventoryRepository.get(player.getUniqueId()).whenComplete((inventory, throwable) -> {
      if (throwable != null) {
        log.error("Failed to load inventory", throwable.getCause());
        kickPlayer(player);
        return;
      }

      if (inventory.isPresent()) {
        ItemStack[] itemStacks;
        try {
          itemStacks = SerializeInventory.toItemStackArray(inventory.get());
        } catch (IOException exception) {
          log.error("Failed to load inventory", exception);
          kickPlayer(player);
          return;
        }
        server.getScheduler().runTask(plugin, () -> player.getInventory().setContents(itemStacks));
      }
    });
  }

  private void kickPlayer(Player player) {
    server.getScheduler().runTask(plugin, () ->
        player.kick(Component.text("Failed to load player data. Please try again")));
  }

  public void saveAllInventories() {
    Map<UUID, Optional<byte[]>> inventories = collectInventories();
    inventoryRepository.saveAll(getPresentInventories(inventories)).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        log.error("Failed to save all inventories", throwable.getCause());
      }
    });
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
