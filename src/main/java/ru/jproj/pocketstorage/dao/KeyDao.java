package ru.jproj.pocketstorage.dao;

import ru.jproj.pocketstorage.entity.Key;
import ru.jproj.pocketstorage.entity.User;
import ru.jproj.pocketstorage.exception.DaoException;
import ru.jproj.pocketstorage.util.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeyDao {

    private static final KeyDao INSTANCE = new KeyDao();
    private static final String SAVE_SQL = """
            INSERT INTO key (user_id, key_hash)
            VALUES (?, ?)
            """;
    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM key
            WHERE id = ?
            """;
    private static final String DELETE_BY_USER_SQL = """
            DELETE FROM key
            WHERE user_id = ?
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id, user_id, key_hash
            FROM key
            WHERE id = ?
            """;
    private static final String FIND_BY_USER_SQL = """
            SELECT id, user_id, key_hash
            FROM key
            WHERE user_id = ?
            """;
    private static final String FIND_KEY_ID_SQL = """
            SELECT id
            FROM key
            WHERE user_id = ? AND key_hash = ?
            """;

    private KeyDao() {};

    public static KeyDao getInstance() {
        return INSTANCE;
    }

    public Key save(Key key) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, key.getUserId());
            preparedStatement.setString(2, key.getKeyHash());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                key.setId(generatedKeys.getLong("id"));
            }
            return key;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public boolean deleteById(Long id) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public boolean deleteAllForUser(User user) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(DELETE_BY_USER_SQL)) {
            preparedStatement.setLong(1, user.getId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<Key> findById(Long id) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();
            Key key = null;
            if (resultSet.next()) {
                key = new Key(resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("key_hash"));
            }
            return Optional.ofNullable(key);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Key> findByUser(User user) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_USER_SQL)) {
            preparedStatement.setLong(1, user.getId());
            var resultSet = preparedStatement.executeQuery();
            List<Key> keys = new ArrayList<>();
            while (resultSet.next()) {
                 keys.add(new Key(resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("key_hash")));
            }
            return keys;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<Long> findId(Long user_id, String keyHash) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(FIND_KEY_ID_SQL)) {
            preparedStatement.setLong(1, user_id);
            preparedStatement.setString(2, keyHash);
            var resultSet = preparedStatement.executeQuery();
            Long id = null;
            if (resultSet.next()) {
                id = resultSet.getLong("id");
            }
            return Optional.ofNullable(id);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

}
