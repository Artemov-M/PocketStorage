package ru.jproj.pocketstorage.dao;

import ru.jproj.pocketstorage.entity.Value;
import ru.jproj.pocketstorage.exception.DaoException;
import ru.jproj.pocketstorage.util.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ValueDao {

    private static final ValueDao INSTANCE = new ValueDao();

    private static final String SAVE_SQL = """
            INSERT INTO value (key_id, value)
            VALUES (?, ?)
            """;
    private static final String DELETE_BY_KEY_ID_SQL = """
            DELETE FROM value
            WHERE key_id = ?
            """;
    private static final String FIND_BY_KEY_ID = """
            SELECT id, key_id, value
            FROM value
            WHERE key_id = ?
            ORDER BY id
            """;
    private ValueDao() {};

    public static ValueDao getInstance() {
        return INSTANCE;
    }

    public Value save(Value value) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, value.getKeyId());
            preparedStatement.setString(2, value.getValue());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                value.setId(generatedKeys.getLong("id"));
            }
            return value;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public boolean delete(Long keyId) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(DELETE_BY_KEY_ID_SQL)) {
            preparedStatement.setLong(1, keyId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Value> findByKeyId(Long keyId) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_KEY_ID)) {
            preparedStatement.setLong(1, keyId);
            var resultSet = preparedStatement.executeQuery();
            List<Value> values = new ArrayList<>();
            while (resultSet.next()) {
                values.add(new Value(resultSet.getLong("id"),
                        resultSet.getLong("key_id"),
                        resultSet.getString("value")));
            }
            return values;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
