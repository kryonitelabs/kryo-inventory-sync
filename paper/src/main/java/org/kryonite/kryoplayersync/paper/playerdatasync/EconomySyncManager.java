package org.kryonite.kryoplayersync.paper.playerdatasync;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.kryonite.kryoplayersync.paper.persistence.EconomyRepository;

@Slf4j
@RequiredArgsConstructor
public class EconomySyncManager {

  private final EconomyRepository economyRepository;
  private final Economy economy;
  private final Server server;
  private final Plugin plugin;

  public CompletableFuture<Void> saveBalance(Player player) {
    return economyRepository.saveBalance(player.getUniqueId(), economy.getBalance(player));
  }

  public void loadBalance(Player player) {
    economyRepository.getBalance(player.getUniqueId()).whenComplete((balance, throwable) -> {
      if (throwable != null) {
        log.error("Failed to load balance", throwable.getCause());
        kickPlayer(player);
        return;
      }

      if (balance.isPresent()) {
        economy.withdrawPlayer(player, economy.getBalance(player));
        economy.depositPlayer(player, balance.get());
      }
    });
  }

  private void kickPlayer(Player player) {
    server.getScheduler().runTask(plugin, () ->
        player.kick(Component.text("Failed to load player data. Please try again")));
  }

  public void saveAllBalances() {
    Map<UUID, Double> balances = collectBalances();
    economyRepository.saveAllBalances(balances).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        log.error("Failed to save all balances", throwable.getCause());
      }
    });
  }

  private Map<UUID, Double> collectBalances() {
    return server.getOnlinePlayers().stream()
        .collect(Collectors.toMap(
            Entity::getUniqueId,
            economy::getBalance
        ));
  }
}
