package net.openright.simpleserverseed.domain.products;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.db.Database.Row;
import net.openright.simpleserverseed.application.SeedAppConfig;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProductRepository {

    private final Sql2o sql2o;

    public ProductRepository(SeedAppConfig config) {
        sql2o = new Sql2o(config.getDataSource(), new PostgresQuirks());
	}

	public void insert(Product product) {
        try (Connection conn = sql2o.open()) {
            int id = (int) conn
                    .createQuery("insert into products (price, active, description, title) values (:price,:active,:description,:title)", true)
                    .addParameter("price", product.getPrice())
                    .addParameter("active", product.isActive())
                    .addParameter("description", product.getDescription())
                    .addParameter("title", product.getTitle())
                    .executeUpdate()
                    .getKey();
            product.setId(id);
        }
	}

    private <T> ResultSetHandler<T> rowMap(Database.RowMapper<T> f) {
        return new ResultSetHandler<T>() {
            @Override
            public T handle(ResultSet resultSet) throws SQLException {
                return f.run(new Row(resultSet));
            }
        };
    }

	public Product retrieve(long id) {
        try (Connection conn = sql2o.open()) {
            return conn.createQuery("select * from products where id = :id")
                    .addParameter("id", id)
                    .executeAndFetchFirst(rowMap(ProductRepository::toProduct));
        }
	}

	public List<Product> list() {
        try (Connection conn = sql2o.open()) {
            return conn.createQuery("select * from products where active = :active order by title")
                    .addParameter("active", true)
                    .executeAndFetch(rowMap(ProductRepository::toProduct));
        }
	}

	public void update(Long id, Product product) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("update products set price = :price, active = :active, description = :description, title = :title where id = :id")
                    .addParameter("price", product.getPrice())
                    .addParameter("active", product.isActive())
                    .addParameter("description", product.getDescription())
                    .addParameter("title", product.getTitle())
                    .addParameter("id", id)
                    .executeUpdate();
        }
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
