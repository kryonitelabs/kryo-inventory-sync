package org.kryonite.kryoplayersync.velocity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kryonite.kryoplayersync.velocity.listener.PlayerListener;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KryoPlayerSyncPluginTest {

  @InjectMocks
  private KryoPlayerSyncPlugin testee;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ProxyServer serverMock;

  @Test
  void shouldSetupListener() {
    // Arrange
    ProxyInitializeEvent proxyInitializeEvent = new ProxyInitializeEvent();

    // Act
    testee.onInitialize(proxyInitializeEvent);

    // Assert
    verify(serverMock.getEventManager()).register(any(), any(PlayerListener.class));
  }
}
