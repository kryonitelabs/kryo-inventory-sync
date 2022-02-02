package org.kryonite.kryoplayersync.paper;

import com.rabbitmq.client.Address;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.kryonite.kryomessaging.api.MessagingService;
import org.kryonite.kryomessaging.service.DefaultActiveMqConnectionFactory;
import org.kryonite.kryomessaging.service.DefaultMessagingService;
import org.kryonite.kryoplayersync.common.PluginMessage;
import org.kryonite.kryoplayersync.paper.listener.PlayerListener;
import org.kryonite.kryoplayersync.paper.messaging.MessagingController;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbInventoryRepository;
import org.kryonite.kryoplayersync.paper.playersync.PlayerSyncManager;
import org.mariadb.jdbc.Driver;

@Slf4j
public class KryoPlayerSyncPlugin extends JavaPlugin {

  private HikariDataSource hikariDataSource;
  private MessagingService messagingService;

  @Override
  public void onEnable() {
    InventoryRepository inventoryRepository;
    try {
      setupHikariDataSource();
      inventoryRepository = new MariaDbInventoryRepository(hikariDataSource);
    } catch (SQLException exception) {
      log.error("Failed to setup InventoryRepository", exception);
      return;
    }

    PlayerSyncManager playerSyncManager = new PlayerSyncManager(inventoryRepository, getServer());

    MessagingController messagingController;
    try {
      messagingController = setupMessagingController(playerSyncManager);
    } catch (IOException | TimeoutException exception) {
      log.error("Failed to setup MessagingService", exception);
      return;
    }

    Messenger messenger = getServer().getMessenger();

    String channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.SAVE_PLAYER_DATA.getValue();
    messenger.registerIncomingPluginChannel(this, channel, (channel1, player, message) -> {
      playerSyncManager.saveInventory(player);
      messagingController.sendInventoryReadyMessage(player.getUniqueId());
      playerSyncManager.addSwitchingServers(player.getUniqueId());
    });

    channel = PluginMessage.NAMESPACE.getValue() + ":" + PluginMessage.INITIAL_JOIN.getValue();
    messenger.registerIncomingPluginChannel(this, channel, (channel1, player, message) -> {
      playerSyncManager.addJoiningPlayer(player.getUniqueId());
    });

    getServer().getPluginManager().registerEvents(new PlayerListener(playerSyncManager), this);
  }

  private MessagingController setupMessagingController(PlayerSyncManager playerSyncManager)
      throws IOException, TimeoutException {
    MessagingController messagingController;
    if (messagingService == null) {
      messagingService = new DefaultMessagingService(new DefaultActiveMqConnectionFactory(
          List.of(Address.parseAddress(getEnv("RABBITMQ_ADDRESS"))),
          getEnv("RABBITMQ_USERNAME"),
          getEnv("RABBITMQ_PASSWORD")
      ));
    }

    messagingController = new MessagingController(
        messagingService,
        getServer(),
        playerSyncManager,
        getEnv("SERVER_NAME")
    );
    messagingController.setupInventoryReady();
    return messagingController;
  }

  private void setupHikariDataSource() throws SQLException {
    if (hikariDataSource == null) {
      DriverManager.registerDriver(new Driver());
      HikariConfig hikariConfig = new HikariConfig();
      hikariConfig.setJdbcUrl(getEnv("CONNECTION_STRING"));
      hikariConfig.setPoolName("kryo-player-sync-pool");
      hikariDataSource = new HikariDataSource(hikariConfig);
    }
  }

  private String getEnv(String name) {
    String connectionString = System.getenv(name);
    if (connectionString == null) {
      connectionString = System.getProperty(name);
    }

    return connectionString;
  }
}
