package org.kryonite.kryoplayersync.paper.playerdatasync;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoplayersync.paper.persistence.EconomyRepository;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EconomySyncManagerTest {

  @InjectMocks
  private EconomySyncManager testee;

  @Mock
  private EconomyRepository economyRepositoryMock;

  @Mock
  private Economy economyMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Server serverMock;

  @Test
  void shouldSaveBalance() throws SQLException {
    // Arrange
    Player player = mock(Player.class);
    UUID uniqueId = UUID.randomUUID();
    double balance = 15;

    when(player.getUniqueId()).thenReturn(uniqueId);
    when(economyMock.getBalance(player)).thenReturn(balance);

    // Act
    testee.saveBalance(player);

    // Assert
    verify(economyRepositoryMock).saveBalance(uniqueId, balance);
  }

  @Test
  void shouldLoadBalance() throws SQLException {
    // Arrange
    Player player = mock(Player.class);
    UUID uniqueId = UUID.randomUUID();
    double oldBalance = 10;
    double balance = 15;

    when(player.getUniqueId()).thenReturn(uniqueId);
    when(economyMock.getBalance(player)).thenReturn(oldBalance);
    when(economyRepositoryMock.getBalance(uniqueId)).thenReturn(Optional.of(balance));

    // Act
    testee.loadBalance(player);

    // Assert
    verify(economyMock).withdrawPlayer(player, oldBalance);
    verify(economyMock).depositPlayer(player, balance);
  }

  @Test
  void shouldSaveAllBalances() throws SQLException {
    // Arrange
    Player player1 = mock(Player.class);
    Player player2 = mock(Player.class);
    UUID uniqueId1 = UUID.randomUUID();
    UUID uniqueId2 = UUID.randomUUID();
    double balance1 = 10;
    double balance2 = 15;

    doReturn(Set.of(player1, player2)).when(serverMock).getOnlinePlayers();

    when(player1.getUniqueId()).thenReturn(uniqueId1);
    when(player2.getUniqueId()).thenReturn(uniqueId2);
    when(economyMock.getBalance(player1)).thenReturn(balance1);
    when(economyMock.getBalance(player2)).thenReturn(balance2);

    // Act
    testee.saveAllBalances();

    // Assert
    verify(economyRepositoryMock).saveAllBalances(Map.of(uniqueId1, balance1, uniqueId2, balance2));
  }
}
