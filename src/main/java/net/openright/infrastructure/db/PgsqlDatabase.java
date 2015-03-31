package net.openright.infrastructure.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openright.infrastructure.util.ExceptionUtil;

public class PgsqlDatabase {

    private static final Logger log = LoggerFactory.getLogger(PgsqlDatabase.class);

    public interface ConnectionCallback<T> {
        T run(Connection conn);
    }

    public interface StatementCallback<T> {
        T run(PreparedStatement stmt) throws SQLException;
    }

    public interface ResultSetMapper<T> {
        T run(ResultSet rs) throws SQLException;
    }

    public interface RowMapper<T> {
        T run(Row row) throws SQLException;
    }

    public interface Inserter {
        void values(Map<String, Object> row);
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

    public class DatabaseTable {

        // parameters are used when building a where clause in a query. Keys corresponds to database columns.
        private final LinkedHashMap<String, Object> parameters;
        private final String tableName;
        private List<String> orderBy = new ArrayList<>();
        private List<String> innerJoins = new ArrayList<>();

        public DatabaseTable(String tableName, LinkedHashMap<String, Object> parameters) {
            this.tableName = tableName;
            this.parameters = new LinkedHashMap<>(parameters);
        }

        /**
         * Used for adding column=value pairs for use in sql.
         * @param tableName
         * @param parameters2
         * @param field column name for use in where clause of sql
         * @param value corresponding to column name for use in where clause of sql
         */
        public DatabaseTable(String tableName, LinkedHashMap<String, Object> parameters, String field, Object value) {
            this(tableName, parameters);
            this.parameters.put(field, value);
        }

        public int insertValues(Inserter inserter) {
            HashMap<String, Object> row = new HashMap<String, Object>();
            inserter.values(row);
            return executeOperation(insertQuery(row.keySet()), row.values(), stmt -> {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                return rs.getInt("id");
            });
        }

        private String insertQuery(Collection<String> columnNames) {
            return "insert into " + tableName + " ("
                + String.join(", ", columnNames) + ") values ("
                + String.join(", ", repeat("?", columnNames.size()))
                + ") returning id";
        }

        public <T> List<T> list(ResultSetMapper<T> mapper) {
            return executeListQuery(getQuery(), parameters.values(), mapper);
        }

        public <T> List<T> list(RowMapper<T> mapper) {
            return executeListQuery(getQuery(), parameters.values(), mapper);
        }

        public <T> T single(RowMapper<T> mapper) {
            return executeQuery(getQuery(), parameters.values(), rs -> {
                if (!rs.next()) {
                    throw new RuntimeException("Not found");
                }
                T result = mapper.run(new Row(rs));
                if (rs.next()) {
                    throw new RuntimeException("Duplicat");
                }
                return result;
            });
        }

        private String getQuery() {
            StringBuilder query = new StringBuilder("select * from ").append(tableName);
            if (!this.innerJoins.isEmpty()) {
                query.append(" ").append(String.join(" ", innerJoins));
            }
            String whereClause = parameters.keySet().stream()
                    .map(s -> s + " = ?")
                    .collect(Collectors.joining(" and "));
            if (!whereClause.isEmpty()) {
                query.append(" where ").append(whereClause);
            }
            if (!orderBy.isEmpty()) {
                query.append(" order by ").append(String.join(",", orderBy));
            }

            return query.toString();
        }

        /**
         * Create a new DatabaseTable object with query parameters for where class. Fluent interface allows for chaining query parameters and values.
         * @param field is the name of the database column
         * @param value
         * @return new instance of self with added set of query parameter and value
         */
        public DatabaseTable where(String field, Object value) {
            return new DatabaseTable(tableName, parameters, field, value);
        }

        public DatabaseTable orderBy(String string) {
            DatabaseTable table = new DatabaseTable(tableName, parameters);
            table.orderBy.add(string);
            return table;
        }

        public DatabaseTable join(String tableName, String myReference, String id) {
            DatabaseTable table = new DatabaseTable(this.tableName, parameters);
            table.innerJoins.add("INNER JOIN " + tableName + " on " + tableName + "." + id
                    + " = " + this.tableName + "." + myReference);
            return table;
        }

    }

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

    private List<String> repeat(String string, int count) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            result.add(string);
        }
        return result;
    }

    public <T> T executeOperation(String query, Collection<Object> parameters, StatementCallback<T> statementCallback) {
        return doWithConnection(conn -> {
            log.info("Executing: {}", query);
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

    public <T> T executeQuery(String query, ResultSetMapper<T> mapper) {
        return executeQuery(query, new ArrayList<Object>(), mapper);
    }

    public <T> List<T> executeListQuery(String query, Collection<Object> parameters, ResultSetMapper<T> mapper) {
        return executeQuery(query, parameters, rs -> {
            List<T> result = new ArrayList<T>();
            while (rs.next()) {
                result.add(mapper.run(rs));
            }
            return result;
        });
    }


    public <T> List<T> executeListQuery(String query, Collection<Object> parameters, RowMapper<T> mapper) {
        return executeQuery(query, parameters, rs -> {
            Row row = new Row(rs);
            List<T> result = new ArrayList<T>();
            while (rs.next()) {
                result.add(mapper.run(row));
            }
            return result;
        });
    }

    public <T> T executeQuery(String query, Collection<Object> parameters, ResultSetMapper<T> mapper) {
        return executeOperation(query, parameters, stmt -> {
            try (ResultSet rs = stmt.executeQuery()) {
                return mapper.run(rs);
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

    private <T> T doWithConnection(ConnectionCallback<T> object) {
        if (threadConnection.get() != null) {
            return object.run(threadConnection.get());
        }

        try (Connection conn = dataSource.getConnection()) {
            return object.run(conn);
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public ResultSet executeQuery(String query) {
        return null;
    }

    public DatabaseTable table(String tableName) {
        return new DatabaseTable(tableName, new LinkedHashMap<>());
    }

}
