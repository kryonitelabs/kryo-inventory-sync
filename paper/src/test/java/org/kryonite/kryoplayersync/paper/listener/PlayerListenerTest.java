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
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerListenerTest {

  @InjectMocks
  private PlayerListener testee;

  @Mock
  private PlayerSyncManager playerSyncManagerMock;

  @Test
  void shouldSyncInventory_WhenPlayerIsJoiningTheNetwork() {
    // Arrange
    Player player = mock(Player.class);
    PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player, Component.empty());

    when(playerSyncManagerMock.removeJoiningPlayer(any())).thenReturn(true);

    // Act
    testee.onPlayerJoin(playerJoinEvent);

    // Assert
    verify(playerSyncManagerMock).syncInventory(player);
  }

  @Test
  void shouldSyncIfReady_WhenPlayerIsSwitchingServers() {
    // Arrange
    Player player = mock(Player.class);
    PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player, Component.empty());

    when(playerSyncManagerMock.removeJoiningPlayer(any())).thenReturn(false);

    // Act
    testee.onPlayerJoin(playerJoinEvent);

    // Assert
    verify(playerSyncManagerMock).syncIfReady(player);
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

    when(playerSyncManagerMock.isSwitchingServers(any())).thenReturn(false);

    // Act
    testee.onPlayerQuit(playerQuitEvent);

    // Assert
    verify(playerSyncManagerMock).saveInventory(player);
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

    when(playerSyncManagerMock.isSwitchingServers(any())).thenReturn(true);

    // Act
    testee.onPlayerQuit(playerQuitEvent);

    // Assert
    verify(playerSyncManagerMock, never()).saveInventory(player);
  }
}
