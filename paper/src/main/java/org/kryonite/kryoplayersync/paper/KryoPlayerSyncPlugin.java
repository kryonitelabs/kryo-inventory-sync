package org.kryonite.kryoplayersync.paper;

import com.rabbitmq.client.Address;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kryonite.kryomessaging.api.MessagingService;
import org.kryonite.kryomessaging.service.DefaultActiveMqConnectionFactory;
import org.kryonite.kryomessaging.service.DefaultMessagingService;
import org.kryonite.kryoplayersync.paper.listener.PlayerListener;
import org.kryonite.kryoplayersync.paper.messaging.MessagingController;
import org.kryonite.kryoplayersync.paper.persistence.EconomyRepository;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;
import org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbEconomyRepository;
import org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbInventoryRepository;
import org.kryonite.kryoplayersync.paper.playerdatasync.EconomySyncManager;
import org.kryonite.kryoplayersync.paper.playerdatasync.InventorySyncManager;
import org.kryonite.kryoplayersync.paper.playerdatasync.PlayerDataSyncManager;
import org.kryonite.kryoplayersync.paper.pluginmessage.PluginMessageManager;
import org.mariadb.jdbc.Driver;

@Slf4j
public class KryoPlayerSyncPlugin extends JavaPlugin {

  private HikariDataSource hikariDataSource;
  private MessagingService messagingService;

  public KryoPlayerSyncPlugin() {
  }

  protected KryoPlayerSyncPlugin(@NotNull JavaPluginLoader loader,
                                 @NotNull PluginDescriptionFile description,
                                 @NotNull File dataFolder,
                                 @NotNull File file,
                                 HikariDataSource hikariDataSource,
                                 MessagingService messagingService) {
    super(loader, description, dataFolder, file);
    this.hikariDataSource = hikariDataSource;
    this.messagingService = messagingService;
  }

  @Override
  public void onEnable() {
    InventoryRepository inventoryRepository = setupInventoryRepository();
    if (inventoryRepository == null) {
      getServer().shutdown();
      return;
    }

    EconomyRepository economyRepository = setupEconomyRepository();
    if (economyRepository == null) {
      getServer().shutdown();
      return;
    }

    Economy economy = setupVault();
    if (economy == null) {
      getServer().shutdown();
      return;
    }

    InventorySyncManager inventorySyncManager = new InventorySyncManager(inventoryRepository, getServer(), this);
    EconomySyncManager economySyncManager = new EconomySyncManager(economyRepository, economy, getServer(), this);

    PlayerDataSyncManager playerDataSyncManager = new PlayerDataSyncManager(
        inventorySyncManager,
        economySyncManager,
        getServer()
    );

    MessagingController messagingController;
    try {
      messagingController = setupMessagingController(playerDataSyncManager);
    } catch (IOException | TimeoutException exception) {
      log.error("Failed to setup MessagingService", exception);
      getServer().shutdown();
      return;
    }

    setupPluginMessages(playerDataSyncManager, messagingController);

    getServer().getPluginManager().registerEvents(new PlayerListener(playerDataSyncManager), this);
  }

  @Nullable
  private InventoryRepository setupInventoryRepository() {
    InventoryRepository inventoryRepository;
    try {
      setupHikariDataSource();
      inventoryRepository = new MariaDbInventoryRepository(hikariDataSource);
    } catch (SQLException exception) {
      log.error("Failed to setup InventoryRepository", exception);
      return null;
    }
    return inventoryRepository;
  }

  @Nullable
  private EconomyRepository setupEconomyRepository() {
    EconomyRepository economyRepository;
    try {
      economyRepository = new MariaDbEconomyRepository(hikariDataSource);
    } catch (SQLException exception) {
      log.error("Failed to setup EconomyRepository", exception);
      return null;
    }
    return economyRepository;
  }

  private MessagingController setupMessagingController(PlayerDataSyncManager playerDataSyncManager)
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
        playerDataSyncManager,
        getEnv("SERVER_NAME")
    );
    messagingController.setupPlayerDataReady();
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

  @Nullable
  private Economy setupVault() {
    if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
      log.error("Vault plugin not enabled!");
      return null;
    }

    RegisteredServiceProvider<Economy> registration = getServer().getServicesManager().getRegistration(Economy.class);
    if (registration == null) {
      log.error("Economy registration not found!");
      return null;
    }

    return registration.getProvider();
  }

  private void setupPluginMessages(PlayerDataSyncManager playerDataSyncManager,
                                   MessagingController messagingController) {
    PluginMessageManager pluginMessageManager = new PluginMessageManager(
        this,
        getServer(),
        playerDataSyncManager,
        messagingController
    );
    pluginMessageManager.setupPluginMessageListener();
  }
}
