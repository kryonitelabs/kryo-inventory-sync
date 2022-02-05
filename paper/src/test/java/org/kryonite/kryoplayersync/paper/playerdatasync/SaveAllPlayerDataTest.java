package org.kryonite.kryoplayersync.paper.playerdatasync;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaveAllPlayerDataTest {

  @InjectMocks
  private SaveAllPlayerData testee;

  @Mock
  private InventorySyncManager inventorySyncManagerMock;

  @Mock
  private EconomySyncManager economySyncManagerMock;

  @Test
  void shouldSaveAllPlayerData() {
    // Arrange - Act
    testee.run();

    // Assert
    verify(inventorySyncManagerMock).saveAllInventories();
    verify(economySyncManagerMock).saveAllBalances();
  }
}
