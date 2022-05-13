package ru.jproj.pocketstorage.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionPool {

    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 5;
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnection;

    static {
        initConnectionPool();
    }

    private static void initConnectionPool() {
        var poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        int size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        sourceConnection = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            var connection = ConnectionManager.open();
            var proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionPool.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals("close")
                            ? pool.add((Connection) proxy) : method.invoke(connection, args));
            pool.add(proxyConnection);
            sourceConnection.add(connection);
        }
    }

    public static Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closePool() {
        try {
            for (Connection connection : sourceConnection) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
