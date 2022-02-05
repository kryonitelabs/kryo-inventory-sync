package org.kryonite.kryoplayersync.paper.playerdatasync;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kryonite.kryoplayersync.paper.persistence.EconomyRepository;

@Slf4j
@RequiredArgsConstructor
public class EconomySyncManager {

  private final EconomyRepository economyRepository;
  private final Economy economy;
  private final Server server;

  public void saveBalance(Player player) {
    try {
      economyRepository.saveBalance(player.getUniqueId(), economy.getBalance(player));
    } catch (SQLException exception) {
      log.error("Failed to save balance", exception);
    }
  }

  public void loadBalance(Player player) {
    try {
      Optional<Double> balance = economyRepository.getBalance(player.getUniqueId());
      if (balance.isPresent()) {
        economy.withdrawPlayer(player, economy.getBalance(player));
        economy.depositPlayer(player, balance.get());
      }
    } catch (SQLException exception) {
      log.error("Failed to load balance", exception);
    }
  }

  public void saveAllBalances() {
    try {
      Map<UUID, Double> balances = collectBalances();
      economyRepository.saveAllBalances(balances);
    } catch (SQLException exception) {
      log.error("Failed to save all balances", exception);
    }
  }

  private Map<UUID, Double> collectBalances() {
    return server.getOnlinePlayers().stream()
        .collect(Collectors.toMap(
            Entity::getUniqueId,
            economy::getBalance
        ));
  }
}
