package org.kryonite.kryoplayersync.velocity.listener;

import static org.kryonite.kryoplayersync.common.PluginMessage.INITIAL_JOIN;
import static org.kryonite.kryoplayersync.common.PluginMessage.NAMESPACE;
import static org.kryonite.kryoplayersync.common.PluginMessage.SAVE_PLAYER_DATA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerListenerTest {

  @InjectMocks
  private PlayerListener testee;

  @Test
  void shouldSendPluginMessageOnPreConnect() {
    // Arrange
    Player player = mock(Player.class);
    RegisteredServer registeredServer = mock(RegisteredServer.class);
    ServerConnection serverConnection = mock(ServerConnection.class);

    when(player.getCurrentServer()).thenReturn(Optional.of(serverConnection));

    ServerPreConnectEvent serverPreConnectEvent = new ServerPreConnectEvent(player, registeredServer);

    // Act
    testee.onServerPreConnect(serverPreConnectEvent);

    // Assert
    verify(serverConnection).sendPluginMessage(
        MinecraftChannelIdentifier.create(NAMESPACE.getValue(), SAVE_PLAYER_DATA.getValue()),
        new byte[] {}
    );
  }

  @Test
  void shouldSendPluginMessageOnFirstPostConnect() {
    // Arrange
    Player player = mock(Player.class);
    ServerConnection serverConnection = mock(ServerConnection.class);

    when(player.getCurrentServer()).thenReturn(Optional.of(serverConnection));

    ServerPostConnectEvent serverPostConnectEvent = new ServerPostConnectEvent(player, null);

    // Act
    testee.onServerPostConnected(serverPostConnectEvent);

    // Assert
    verify(serverConnection).sendPluginMessage(
        MinecraftChannelIdentifier.create(NAMESPACE.getValue(), INITIAL_JOIN.getValue()),
        new byte[] {}
    );
  }

  @Test
  void shouldNotSendPluginMessageOnNormalPostConnect() {
    // Arrange
    Player player = mock(Player.class);
    RegisteredServer registeredServer = mock(RegisteredServer.class);
    ServerConnection serverConnection = mock(ServerConnection.class);

    ServerPostConnectEvent serverPostConnectEvent = new ServerPostConnectEvent(player, registeredServer);

    // Act
    testee.onServerPostConnected(serverPostConnectEvent);

    // Assert
    verify(serverConnection, never()).sendPluginMessage(any(), any());
  }
}
