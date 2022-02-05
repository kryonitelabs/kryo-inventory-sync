package org.kryonite.kryoplayersync.paper.playerdatasync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class SaveAllPlayerData implements Runnable {

  private final InventorySyncManager inventorySyncManager;
  private final EconomySyncManager economySyncManager;

  @Override
  public void run() {
    try {
      inventorySyncManager.saveAllInventories();
      economySyncManager.saveAllBalances();
    } catch (Exception exception) {
      log.error("Failed to save all PlayerData", exception);
    }
  }
}
