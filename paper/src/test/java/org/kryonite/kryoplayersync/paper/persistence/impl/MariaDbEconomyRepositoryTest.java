package org.kryonite.kryoplayersync.paper.persistence.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbEconomyRepository.CREATE_ECONOMY_TABLE;
import static org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbEconomyRepository.GET_BALANCE;
import static org.kryonite.kryoplayersync.paper.persistence.impl.MariaDbEconomyRepository.INSERT_BALANCE;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MariaDbEconomyRepositoryTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HikariDataSource dataSourceMock;

  @Test
  void shouldCreateTableOnStartup() throws SQLException {
    // Arrange - Act
    new MariaDbEconomyRepository(dataSourceMock);

    // Assert
    verify(dataSourceMock.getConnection()).prepareStatement(CREATE_ECONOMY_TABLE);
  }

  @Test
  void shouldSaveBalance() throws SQLException, ExecutionException, InterruptedException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    double balance = 12;

    MariaDbEconomyRepository testee = new MariaDbEconomyRepository(dataSourceMock);

    // Act
    testee.saveBalance(uniqueId, balance).get();

    // Assert
    verify(dataSourceMock.getConnection()).prepareStatement(INSERT_BALANCE);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setString(1, uniqueId.toString());
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setDouble(2, balance);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setDouble(3, balance);

    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).executeUpdate();
  }

  @Test
  void shouldThrowException_WhenSaveBalanceFails() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    double balance = 12;

    MariaDbEconomyRepository testee = new MariaDbEconomyRepository(dataSourceMock);
    when(dataSourceMock.getConnection()).thenThrow(new SQLException());

    // Act - Assert
    assertThrows(ExecutionException.class, () -> testee.saveBalance(uniqueId, balance).get());
  }

  @Test
  void shouldReturnBalance() throws SQLException, ExecutionException, InterruptedException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();
    double balance = 15;

    MariaDbEconomyRepository testee = new MariaDbEconomyRepository(dataSourceMock);
    when(dataSourceMock.getConnection().prepareStatement(GET_BALANCE).executeQuery().first()).thenReturn(true);
    when(dataSourceMock.getConnection().prepareStatement(GET_BALANCE).executeQuery().getDouble("balance"))
        .thenReturn(balance);

    // Act
    Optional<Double> result = testee.getBalance(uniqueId).get();

    // Assert
    assertTrue(result.isPresent());
    assertEquals(balance, result.get());
    verify(dataSourceMock.getConnection(), atLeastOnce()).prepareStatement(GET_BALANCE);
    verify(dataSourceMock.getConnection().prepareStatement(GET_BALANCE)).setString(1, uniqueId.toString());
  }

  @Test
  void shouldThrowException_WhenReturnBalanceFails() throws SQLException {
    // Arrange
    UUID uniqueId = UUID.randomUUID();

    MariaDbEconomyRepository testee = new MariaDbEconomyRepository(dataSourceMock);
    when(dataSourceMock.getConnection()).thenThrow(new SQLException());

    // Act - Assert
    assertThrows(ExecutionException.class, () -> testee.getBalance(uniqueId).get());
  }

  @Test
  void shouldSaveAllBalances() throws SQLException, ExecutionException, InterruptedException {
    // Arrange
    UUID uniqueId1 = UUID.randomUUID();
    UUID uniqueId2 = UUID.randomUUID();
    double balance1 = 14;
    double balance2 = 22;

    MariaDbEconomyRepository testee = new MariaDbEconomyRepository(dataSourceMock);

    // Act
    testee.saveAllBalances(Map.of(uniqueId1, balance1, uniqueId2, balance2)).get();

    // Assert
    verify(dataSourceMock.getConnection()).prepareStatement(INSERT_BALANCE);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setString(1, uniqueId1.toString());
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setDouble(2, balance1);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setDouble(3, balance1);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setString(1, uniqueId2.toString());
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setDouble(2, balance2);
    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).setDouble(3, balance2);

    verify(dataSourceMock.getConnection().prepareStatement(INSERT_BALANCE)).executeBatch();
    verify(dataSourceMock.getConnection()).commit();
  }

  @Test
  void shouldThrowException_WhenSaveAllBalancesFails() throws SQLException {
    // Arrange
    UUID uniqueId1 = UUID.randomUUID();
    UUID uniqueId2 = UUID.randomUUID();
    double balance1 = 14;
    double balance2 = 22;

    MariaDbEconomyRepository testee = new MariaDbEconomyRepository(dataSourceMock);
    when(dataSourceMock.getConnection()).thenThrow(new SQLException());

    // Act - Assert
    assertThrows(ExecutionException.class,
        () -> testee.saveAllBalances(Map.of(uniqueId1, balance1, uniqueId2, balance2)).get());
  }
}
