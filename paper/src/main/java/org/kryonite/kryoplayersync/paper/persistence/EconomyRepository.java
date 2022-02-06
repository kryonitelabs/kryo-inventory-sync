package org.kryonite.kryoplayersync.paper.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EconomyRepository {

  CompletableFuture<Optional<Double>> getBalance(UUID uniqueId);

  CompletableFuture<Void> saveBalance(UUID uniqueId, double balance);

  CompletableFuture<Void> saveAllBalances(Map<UUID, Double> balances);
}
