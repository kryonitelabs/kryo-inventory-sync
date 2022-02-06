package org.kryonite.kryoplayersync.paper.playerdatasync;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.awaitility.Awaitility;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private EconomyRepository economyRepositoryMock;

  @Mock
  private Economy economyMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Server serverMock;

  @Mock
  private Plugin pluginMock;

  @Test
  void shouldSaveBalance() {
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
  void shouldLoadBalance() {
    // Arrange
    Player player = mock(Player.class);
    UUID uniqueId = UUID.randomUUID();
    double oldBalance = 10;
    double balance = 15;

    when(player.getUniqueId()).thenReturn(uniqueId);
    when(economyMock.getBalance(player)).thenReturn(oldBalance);
    when(economyRepositoryMock.getBalance(uniqueId))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(balance)));

    // Act
    testee.loadBalance(player);

    // Assert
    verify(economyMock).withdrawPlayer(player, oldBalance);
    verify(economyMock).depositPlayer(player, balance);
  }

  @Test
  void shouldKickPlayer_WhenLoadBalanceFails() {
    // Arrange
    Player player = mock(Player.class, Answers.RETURNS_DEEP_STUBS);
    UUID uniqueId = UUID.randomUUID();

    when(player.getUniqueId()).thenReturn(uniqueId);
    when(economyRepositoryMock.getBalance(uniqueId)).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));

    // Act
    testee.loadBalance(player);

    // Assert
    Awaitility.await()
        .atMost(1, TimeUnit.SECONDS)
        .untilAsserted(() -> serverMock.getScheduler().runTask(pluginMock,
            () -> player.kick(Component.text("Failed to load player data. Please try again"))));
  }

  @Test
  void shouldSaveAllBalances() {
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
    when(economyRepositoryMock.saveAllBalances(anyMap())).thenReturn(CompletableFuture.completedFuture(null));

    // Act
    testee.saveAllBalances();

    // Assert
    verify(economyRepositoryMock).saveAllBalances(Map.of(uniqueId1, balance1, uniqueId2, balance2));
  }
}
