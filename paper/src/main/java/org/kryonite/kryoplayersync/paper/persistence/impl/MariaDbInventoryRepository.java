package org.kryonite.kryoplayersync.paper.persistence.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.kryonite.kryoplayersync.paper.persistence.InventoryRepository;

public class MariaDbInventoryRepository implements InventoryRepository {

  protected static final String CREATE_INVENTORY_TABLE =
      "CREATE TABLE IF NOT EXISTS inventory "
          + "(minecraft_uuid varchar(36) primary key, "
          + "data blob not null)";
  protected static final String INSERT_INVENTORY =
      "INSERT INTO inventory (minecraft_uuid, data) VALUES (?, ?) "
          + "ON DUPLICATE KEY UPDATE data=?";
  protected static final String GET_INVENTORY = "SELECT * FROM inventory WHERE minecraft_uuid = ?";

  private final HikariDataSource dataSource;

  public MariaDbInventoryRepository(HikariDataSource dataSource) throws SQLException {
    this.dataSource = dataSource;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement createTable = connection.prepareStatement(CREATE_INVENTORY_TABLE)) {
      createTable.executeUpdate();
    }
  }

  @Override
  public void save(UUID uniqueId, byte[] inventory) throws SQLException {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INVENTORY)) {
      preparedStatement.setString(1, uniqueId.toString());
      preparedStatement.setBytes(2, inventory);
      preparedStatement.setBytes(3, inventory);

      preparedStatement.executeUpdate();
    }
  }

  @Override
  public Optional<byte[]> get(UUID uniqueId) throws SQLException {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(GET_INVENTORY)) {
      preparedStatement.setString(1, uniqueId.toString());

      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.first()) {
        return Optional.of(resultSet.getBytes("data"));
      }
      return Optional.empty();
    }
  }
}
