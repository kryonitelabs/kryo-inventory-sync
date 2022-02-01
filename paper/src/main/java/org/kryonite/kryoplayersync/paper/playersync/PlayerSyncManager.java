package org.kryonite.kryoplayersync.paper.playersync;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlayerSyncManager {

  private final Map<UUID, Long> joiningPlayers = new ConcurrentHashMap<>();

  public PlayerSyncManager() {
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      joiningPlayers.values().removeIf(timestamp -> timestamp + 5000 < now);
      System.out.println(joiningPlayers.size());
    }, 1, 1, TimeUnit.SECONDS);
  }

  public void addJoiningPlayer(UUID uniqueId) {
    joiningPlayers.put(uniqueId, System.currentTimeMillis());
  }

  public boolean removeJoiningPlayer(UUID uniqueId) {
    return joiningPlayers.remove(uniqueId) != null;
  }
}
