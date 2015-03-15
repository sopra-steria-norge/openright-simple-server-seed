package net.openright.infrastructure.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class Database {

	public interface ConnectionCallback<T> {

		T run(Connection conn);

	}

	public interface ResultSetMapper<T> {

		T run(ResultSet rs) throws SQLException;
		
	}

	public interface Inserter {
		
		void values(Map<String, Object> row);

	}

	public class DatabaseTable {

		private String tableName;

		public DatabaseTable(String tableName) {
			this.tableName = tableName;
			// TODO Auto-generated constructor stub
		}

		public void insertValues(Inserter inserter) {
			HashMap<String, Object> row = new HashMap<String, Object>();
			inserter.values(row);
			executeOperation(insertQuery(row.keySet()), row.values());
			
		}

		private String insertQuery(Collection<String> columnNames) {
			return "insert into " + tableName + " (" 
				+ String.join(", ", columnNames) + ") values ("
				+ String.join(", ", repeat("?", columnNames.size()))
				+ ")";
		}

		public <T> List<T> list(ResultSetMapper<T> mapper) {
			return executeQuery(getQuery(), rs -> {
				ArrayList<T> result = new ArrayList<T>();
				while (rs.next()) {
					result.add(mapper.run(rs));
				}
				return result;
			});
		}

		private String getQuery() {
			return "select * from " + tableName;
		}

	}

	private final DataSource dataSource;


	public Database(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private List<String> repeat(String string, int count) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			result.add(string);
		}
		return result;
	}

	public void executeOperation(String query, Collection<Object> parameters) {
		doWithConnection(conn -> {
			try (PreparedStatement prepareStatement = conn.prepareStatement(query)) {
				int index = 1;
				for (Object object : parameters) {
					prepareStatement.setObject(index++, object);
				}
				
				prepareStatement.execute();
				return null;
			} catch (SQLException e) {
				throw convertException(e);
			}
		});
		
	}

	public <T> List<T> executeQuery(String query, ResultSetMapper<List<T>> object) {
		return doWithConnection(conn -> {
			try (PreparedStatement prepareStatement = conn.prepareStatement(query)) {
				return object.run(prepareStatement.executeQuery());
			} catch (SQLException e) {
				throw convertException(e);
			}
		});
	}

	private <T> T doWithConnection(ConnectionCallback<T> object) {
		try (Connection conn = getConnection()) {
			return object.run(conn);
		} catch (SQLException e) {
			throw convertException(e);
		}
	}

	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public ResultSet executeQuery(String query) {
		return null;
	}

	public DatabaseTable table(String tableName) {
		return new DatabaseTable(tableName);
	}

	public static RuntimeException convertException(SQLException e) {
		return new RuntimeException(e);
	}

}
