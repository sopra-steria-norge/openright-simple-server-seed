package net.openright.simpleserverseed.domain.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.db.PgsqlDatabase.DatabaseTable;

public class ProductRepository {

	private DatabaseTable table;

	public ProductRepository(PgsqlDatabase database) {
		table = database.table("products");
	}

	public void insert(Product product) {
		long id = table.insertValues((row) -> {
			row.put("title", product.getTitle());
			row.put("active", product.isActive());
			row.put("price", product.getPrice());
		});
		product.setId(id);
	}

	public Product retrieve(long id) {
		return table.where("id", id).single(this::toProduct);
	}

	public List<Product> list() {
		return table.where("active", true)
				.orderBy("title")
				.list(this::toProduct);
	}

	private Product toProduct(ResultSet rs) throws SQLException {
		Product product = new Product();
		product.setId(rs.getLong("id"));
		product.setTitle(rs.getString("title"));
		product.setDescription(rs.getString("description"));
		product.setActive(rs.getBoolean("active"));
		product.setPrice(rs.getDouble("price"));
		return product;
	}


}
