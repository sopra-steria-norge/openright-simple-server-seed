package net.openright.simpleserverseed.domain.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.db.PgsqlDatabase.Row;
import net.openright.infrastructure.rest.RequestException;

public class ProductRepository {

	private PgsqlDatabase db;

	public ProductRepository(PgsqlDatabase database) {
		db = database;
	}

	public void insert(Product product) {
		String query = "insert into products (price, active, description, title) values (?,?,?,?) returning id";
		Object[] parameters = new Object[] { product.getPrice(), product.isActive(), product.getDescription(),
				product.getTitle() };

		product.setId(db.executeDbOperation(query, parameters, stmt -> {
			ResultSet rs = stmt.executeQuery();
			rs.next();
			return rs.getLong("id");
		}));
	}

	public Product retrieve(long id) {
		String query = "select * from products where id = ?";
		Object[] parameters = new Object[] { id };

		return db.executeDbOperation(query, parameters, stmt -> {
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new RequestException(404, "Order " + id + " not found");
				}
				Product result = toProduct(new Row(rs));
				if (rs.next()) {
					throw new RuntimeException("Duplicate");
				}
				return result;
			}
		});
	}

	public List<Product> list() {
		String query = "select * from products where active = ? order by title";
		Object[] parameters = new Object[] { true };

		return db.executeDbOperation(query, parameters, stmt -> {
			try (ResultSet rs = stmt.executeQuery()) {
				List<Product> result = new ArrayList<Product>();
				while (rs.next()) {
					result.add(toProduct(new Row(rs)));
				}
				return result;
			}
		});
	}

	public void update(Long id, Product product) {
		String query = "update products set price = ?, active = ?, description = ?, title = ? where id = ?";
		Object[] parameters = new Object[] { product.getPrice(), product.isActive(), product.getDescription(),
				product.getTitle(), id };

		db.executeDbOperation(query, parameters, stmt -> {
			stmt.executeUpdate();
			return null;
		});
	}

	public static Product toProduct(Row rs) throws SQLException {
		Product product = new Product();
		product.setId(rs.getLong("products", "id"));
		product.setTitle(rs.getString("products", "title"));
		product.setDescription(rs.getString("products", "description"));
		product.setActive(rs.getBoolean("products", "active"));
		product.setPrice(rs.getDouble("products", "price"));
		return product;
	}

}
