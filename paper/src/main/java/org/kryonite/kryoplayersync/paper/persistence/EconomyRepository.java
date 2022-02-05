package org.kryonite.kryoplayersync.paper.persistence;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface EconomyRepository {

  Optional<Double> getBalance(UUID uniqueId) throws SQLException;

  void saveBalance(UUID uniqueId, double balance) throws SQLException;

  void saveAllBalances(Map<UUID, Double> balances) throws SQLException;
}
