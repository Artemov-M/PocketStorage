package ru.jproj.pocketstorage.dao;

import ru.jproj.pocketstorage.entity.User;
import ru.jproj.pocketstorage.exception.DaoException;
import ru.jproj.pocketstorage.util.ConnectionPool;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDao {

    private static final UserDao INSTANCE = new UserDao();
    private static final String DELETE_SQL = """
            DELETE FROM users
            WHERE id = ?
            """;
    private static final String SAVE_SQL = """
            INSERT INTO users (user_hash)
            VALUES (?)
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT id,
                user_hash
            FROM users
            WHERE id = ?
            """;
    private static final String FIND_BY_HASH_SQL = """
            SELECT id,
                user_hash
            FROM users
            WHERE user_hash = ?
            """;

    private UserDao() {
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }

    public boolean delete(Long id) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public User save(User user) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getUserHash());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getLong("id"));
            }
            return user;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public User save(String userHash) {
        return save(new User(-1L, userHash));
    }

    public Optional<User> findById(Long id) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();
            User user = null;
            if (resultSet.next()) {
                user = new User(resultSet.getLong("id"),
                        resultSet.getString("user_hash"));
            }
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<User> findByHash(String hash) {
        try (var connection = ConnectionPool.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_HASH_SQL)) {
            preparedStatement.setString(1, hash);
            var resultSet = preparedStatement.executeQuery();
            User user = null;
            if (resultSet.next()) {
                user = new User(resultSet.getLong("id"),
                        resultSet.getString("user_hash"));
            }
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

}
