package org.kryonite.kryoplayersync.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.kryonite.kryoplayersync.velocity.listener.PlayerListener;

@Plugin(id = "kryo-player-sync", name = "Kryo Player Sync", authors = "Kryonite Labs", version = "0.1.0")
public class KryoPlayerSyncPlugin {

  private final ProxyServer server;

  @Inject
  public KryoPlayerSyncPlugin(ProxyServer server) {
    this.server = server;
  }

  @Subscribe
  public void onInitialize(ProxyInitializeEvent event) {
    server.getEventManager().register(this, new PlayerListener());
  }
}
