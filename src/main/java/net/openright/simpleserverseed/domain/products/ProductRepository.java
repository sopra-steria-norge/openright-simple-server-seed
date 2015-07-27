package net.openright.simpleserverseed.domain.products;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.db.Database.Row;
import net.openright.infrastructure.rest.RequestException;
import net.openright.simpleserverseed.application.SeedAppConfig;

import java.sql.SQLException;
import java.util.List;

public class ProductRepository {

    private SeedAppConfig config;

	public ProductRepository(SeedAppConfig config) {
		this.config = config;
	}

	public void insert(Product product) {
		product.setId(getDb().insert("insert into products (price, active, description, title) values (?,?,?,?) returning id",
				product.getPrice(), product.isActive(), product.getDescription(), product.getTitle()));
	}

	public Product retrieve(long id) {
		return getDb().queryForSingle("select * from products where id = ?", id, ProductRepository::toProduct)
				.orElseThrow(() -> new RequestException(404, "Order " + id + " not found"));
	}

	public List<Product> list() {
		return getDb().queryForList("select * from products where active = ? order by title",
				ProductRepository::toProduct, true);
	}

	public void update(Long id, Product product) {
		getDb().executeOperation("update products set price = ?, active = ?, description = ?, title = ? where id = ?",
				product.getPrice(), product.isActive(), product.getDescription(), product.getTitle(), id);
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

    public Database getDb() {
        return config.getDatabase();
    }

}
