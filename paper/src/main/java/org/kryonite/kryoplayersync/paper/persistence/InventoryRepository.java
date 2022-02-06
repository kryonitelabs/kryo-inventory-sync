package org.kryonite.kryoplayersync.paper.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface InventoryRepository {

  CompletableFuture<Void> save(UUID uniqueId, byte[] inventory);

  CompletableFuture<Void> saveAll(Map<UUID, byte[]> inventories);

  CompletableFuture<Optional<byte[]>> get(UUID uniqueId);
}
