package org.kryonite.kryoplayersync.paper.persistence.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.kryonite.kryoplayersync.paper.persistence.EconomyRepository;

public class MariaDbEconomyRepository implements EconomyRepository {

  protected static final String CREATE_ECONOMY_TABLE =
      "CREATE TABLE IF NOT EXISTS economy "
          + "(minecraft_uuid varchar(36) primary key, "
          + "balance double not null)";
  protected static final String INSERT_BALANCE =
      "INSERT INTO economy (minecraft_uuid, balance) VALUES (?, ?) "
          + "ON DUPLICATE KEY UPDATE balance=?";
  protected static final String GET_BALANCE = "SELECT * FROM economy WHERE minecraft_uuid = ?";

  private final HikariDataSource dataSource;

  public MariaDbEconomyRepository(HikariDataSource dataSource) throws SQLException {
    this.dataSource = dataSource;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement createTable = connection.prepareStatement(CREATE_ECONOMY_TABLE)) {
      createTable.executeUpdate();
    }
  }

  @Override
  public CompletableFuture<Void> saveBalance(UUID uniqueId, double balance) {
    return CompletableFuture.runAsync(() -> {
      try (Connection connection = dataSource.getConnection();
           PreparedStatement preparedStatement = connection.prepareStatement(INSERT_BALANCE)) {
        preparedStatement.setString(1, uniqueId.toString());
        preparedStatement.setDouble(2, balance);
        preparedStatement.setDouble(3, balance);

        preparedStatement.executeUpdate();
      } catch (SQLException exception) {
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public CompletableFuture<Void> saveAllBalances(Map<UUID, Double> balances) {
    return CompletableFuture.runAsync(() -> {
      try (Connection connection = dataSource.getConnection();
           PreparedStatement preparedStatement = connection.prepareStatement(INSERT_BALANCE)) {
        connection.setAutoCommit(false);

        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
          preparedStatement.setString(1, entry.getKey().toString());
          preparedStatement.setDouble(2, entry.getValue());
          preparedStatement.setDouble(3, entry.getValue());
          preparedStatement.addBatch();
        }

        preparedStatement.executeBatch();
        connection.commit();
        connection.setAutoCommit(true);
      } catch (SQLException exception) {
        throw new CompletionException(exception);
      }
    });
  }

  @Override
  public CompletableFuture<Optional<Double>> getBalance(UUID uniqueId) {
    return CompletableFuture.supplyAsync(() -> {
      try (Connection connection = dataSource.getConnection();
           PreparedStatement preparedStatement = connection.prepareStatement(GET_BALANCE)) {
        preparedStatement.setString(1, uniqueId.toString());

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.first()) {
          return Optional.of(resultSet.getDouble("balance"));
        }
        return Optional.empty();
      } catch (SQLException exception) {
        throw new CompletionException(exception);
      }
    });
  }
}
