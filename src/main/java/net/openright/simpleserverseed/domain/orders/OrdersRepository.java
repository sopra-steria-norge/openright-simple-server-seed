package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.rest.RequestException;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.domain.products.ProductRepository;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class OrdersRepository {

	private final Sql2o sql2o;

	OrdersRepository(SeedAppConfig config) {
		sql2o = new Sql2o(config.getDataSource(), new PostgresQuirks());
	}

	private <T> ResultSetHandler<T> rowMap(Database.RowMapper<T> f) {
		return new ResultSetHandler<T>() {
			@Override
			public T handle(ResultSet resultSet) throws SQLException {
				return f.run(new Database.Row(resultSet));
			}
		};
	}

	List<Order> list() {
		try (Connection conn = sql2o.open()) {
			return conn.createQuery("select * from orders order by title")
					.executeAndFetch(rowMap(this::toOrder));
		}
	}

	Order retrieve(int id) {
		try (Connection conn = sql2o.open()) {
			Order result = conn.createQuery("select * from orders where id = :id")
					.addParameter("id", id)
					.executeAndFetchFirst(rowMap(row -> toOrderWithOrderLines(id, row)));
			if (result == null) {
				throw new RequestException(404, "Can't find Order with id " + id);
			}
			return result;
		}
	}

	void insert(Order order) {
		validateOrder(order);

		sql2o.runInTransaction((conn, o) -> {
            int id = (int)conn.createQuery("insert into orders (title) values (:title)", true)
                    .addParameter("title", order.getTitle())
                    .executeUpdate()
                    .getKey();
			order.setId(id);
			insertOrderLines(conn, order.getId(), order);
		});
	}

	public void update(int orderId, Order order) {
		validateOrder(order);

		sql2o.runInTransaction((conn, o) -> {
			updateOrder(conn, orderId, order);
			deleteOrderLines(conn, orderId);
			insertOrderLines(conn, orderId, order);
		});
	}


	private List<OrderLine> queryForOrderLines(int orderId) {
        try (Connection conn = sql2o.open()) {
            List<OrderLine> result = conn.createQuery("select * from order_lines INNER JOIN products on products.id = order_lines.product_id where order_id = :order_id")
                    .addParameter("order_id", orderId)
                    .executeAndFetch(rowMap(row -> toOrderLine(row)));
            if (result == null) {
                throw new RequestException("");
            }
            return result;
        }
	}

	private void deleteOrderLines(Connection conn, int orderId) {
		conn.createQuery("delete from order_lines where order_id = :id")
				.addParameter("id", orderId)
				.executeUpdate();
	}

	private void updateOrder(Connection conn, int orderId, Order order) {
		conn.createQuery("update orders set title = :title where id = :id")
				.addParameter("id", orderId)
				.addParameter("title", order.getTitle())
				.executeUpdate();
	}

	private void insertOrderLines(Connection conn, int orderId, Order order) {
		for (OrderLine orderLine : order.getOrderLines()) {
			conn.createQuery("insert into order_lines (amount, product_id, title, order_id) values (:amount, :product_id, :title, :order_id)")
					.addParameter("amount", orderLine.getAmount())
					.addParameter("product_id", orderLine.getProductId())
					.addParameter("title", orderLine.getTitle())
					.addParameter("order_id", orderId)
					.executeUpdate();
		}
	}

	private void validateOrder(Order order) {
		if (order.getTitle().equals("null")) {
			throw new RuntimeException("Null title is invalid");
		}
		if (order.getTitle().contains("foul")) {
			throw new RequestException("No foul language in orders, please!");
		}
	}

    private Order toOrderWithOrderLines(int id, Database.Row row) throws SQLException {
        return toOrder(row).withOrderLines(queryForOrderLines(id));
    }

	private Order toOrder(Database.Row row) throws SQLException {
		Order order = new Order(row.getString("title"));
		order.setId(row.getInt("id"));
		return order;
	}

	private OrderLine toOrderLine(Database.Row row) throws SQLException {
		OrderLine orderLine = new OrderLine(row.getString("title"));
		orderLine.setProduct(ProductRepository.toProduct(row));
		orderLine.setAmount(row.getInt("amount"));
		return orderLine;
	}
}
