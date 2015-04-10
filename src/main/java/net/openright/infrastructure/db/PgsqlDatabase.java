package net.openright.infrastructure.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.openright.infrastructure.util.ExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PgsqlDatabase {

    private static final Logger log = LoggerFactory.getLogger(PgsqlDatabase.class);

    private final DataSource dataSource;
    private final static ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    public PgsqlDatabase(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PgsqlDatabase(String name) {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup(name);
        } catch (NamingException e) {
            throw ExceptionUtil.soften(e);
        }
    }
    
    public <T> T executeDbOperation(String query, Object[] parameters, StatementCallback<T> statementCallback) {
        return doWithConnection(conn -> {
            log.info("Executing: {} with params {}", query, parameters.toString());
            try (PreparedStatement prepareStatement = conn.prepareStatement(query)) {
                int index = 1;
                for (Object object : parameters) {
                    prepareStatement.setObject(index++, object);
                }

                return statementCallback.run(prepareStatement);
            } catch (SQLException e) {
                throw ExceptionUtil.soften(e);
            }
        });
    }
    
    public void doInTransaction(Runnable operation) {
        try (Connection connection = dataSource.getConnection()) {
            threadConnection.set(connection);
            try {
                operation.run();
            } finally {
                threadConnection.set(null);
            }
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public <T> T doWithConnection(ConnectionCallback<T> object) {
        if (threadConnection.get() != null) {
            return object.run(threadConnection.get());
        }

        try (Connection conn = dataSource.getConnection()) {
            return object.run(conn);
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }
    
    public static class Row {

        private ResultSet rs;
        private Map<String, Integer> columnMap = new HashMap<>();

        public Row(ResultSet rs) throws SQLException {
            this.rs = rs;
            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                String tableName = rs.getMetaData().getTableName(i);
                String columnName = rs.getMetaData().getColumnName(i);

                this.columnMap.put(tableName + "." + columnName, i);
            }
        }

        public String getString(String string) throws SQLException {
            return rs.getString(string);
        }

        public long getLong(String columnName) throws SQLException {
            return rs.getLong(columnName);
        }

        public int getInt(String columnName) throws SQLException {
            return rs.getInt(columnName);
        }

        public boolean getBoolean(String columnName) throws SQLException {
            return rs.getBoolean(columnName);
        }

        public double getDouble(String columnLabel) throws SQLException {
            return rs.getDouble(columnLabel);
        }

        public long getLong(String tableName, String columnName) throws SQLException {
            return rs.getLong(getColumnIndex(tableName, columnName));
        }

        public String getString(String tableName, String columnName) throws SQLException {
            return rs.getString(getColumnIndex(tableName, columnName));
        }

        public boolean getBoolean(String tableName, String columnName) throws SQLException {
            return rs.getBoolean(getColumnIndex(tableName, columnName));
        }

        public double getDouble(String tableName, String columnName) throws SQLException {
            return rs.getDouble(getColumnIndex(tableName, columnName));
        }

        private int getColumnIndex(String tableName, String columnName) {
            return columnMap.get(tableName + "." + columnName);
        }
    }
    
    public interface ConnectionCallback<T> {
        T run(Connection conn);
    }

    public interface StatementCallback<T> {
        T run(PreparedStatement stmt) throws SQLException;
    }
}
