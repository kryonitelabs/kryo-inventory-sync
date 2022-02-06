package org.kryonite.kryoplayersync.paper.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerListenerTest {

  @InjectMocks
  private PlayerListener testee;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PlayerDataSyncManager playerDataSyncManagerMock;

  @Test
  void shouldSyncInventory_WhenPlayerIsJoiningTheNetwork() {
    // Arrange
    Player player = mock(Player.class);
    PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player, Component.empty());

    when(playerDataSyncManagerMock.removeJoiningPlayer(any())).thenReturn(true);

    // Act
    testee.onPlayerJoin(playerJoinEvent);

    // Assert
    verify(playerDataSyncManagerMock).loadPlayerData(player);
  }

  @Test
  void shouldSyncIfReady_WhenPlayerIsSwitchingServers() {
    // Arrange
    Player player = mock(Player.class);
    PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player, Component.empty());

    when(playerDataSyncManagerMock.removeJoiningPlayer(any())).thenReturn(false);

    // Act
    testee.onPlayerJoin(playerJoinEvent);

    // Assert
    verify(playerDataSyncManagerMock).syncIfReady(player);
  }

  @Test
  void shouldSyncInventory_WhenPlayerQuitsTheNetwork() {
    // Arrange
    Player player = mock(Player.class);
    PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(
        player,
        Component.empty(),
        PlayerQuitEvent.QuitReason.DISCONNECTED
    );

    when(playerDataSyncManagerMock.isSwitchingServers(any())).thenReturn(false);

    // Act
    testee.onPlayerQuit(playerQuitEvent);

    // Assert
    verify(playerDataSyncManagerMock).savePlayerData(player);
  }

  @Test
  void shouldNotSyncInventory_WhenPlayerSwitchesServers() {
    // Arrange
    Player player = mock(Player.class);
    PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(
        player,
        Component.empty(),
        PlayerQuitEvent.QuitReason.DISCONNECTED
    );

    when(playerDataSyncManagerMock.isSwitchingServers(any())).thenReturn(true);

    // Act
    testee.onPlayerQuit(playerQuitEvent);

    // Assert
    verify(playerDataSyncManagerMock, never()).savePlayerData(player);
  }
}
