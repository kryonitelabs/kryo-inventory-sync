package org.kryonite.kryoplayersync.paper.pluginmessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoplayersync.common.PluginMessage;
import org.kryonite.kryoplayersync.paper.messaging.MessagingController;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PluginMessageManagerTest {

  @InjectMocks
  private PluginMessageManager testee;

  @Mock
  private Plugin pluginMock;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Server serverMock;

  @Mock
  private PlayerDataSyncManager playerDataSyncManagerMock;

  @Mock
  private MessagingController messagingControllerMock;

  @Test
  void shouldSetupPluginMessageListener() {
    // Arrange
    String channel1 = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.SAVE_PLAYER_DATA.getValue();
    String channel2 = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.INITIAL_JOIN.getValue();

    // Act
    testee.setupPluginMessageListener();

    // Assert
    verify(serverMock.getMessenger()).registerIncomingPluginChannel(eq(pluginMock), eq(channel1), any());
    verify(serverMock.getMessenger()).registerIncomingPluginChannel(eq(pluginMock), eq(channel2), any());
  }
}
