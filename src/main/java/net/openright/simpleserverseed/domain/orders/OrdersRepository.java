package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.rest.RequestException;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.domain.products.ProductRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

class OrdersRepository {

    private SeedAppConfig config;

	OrdersRepository(SeedAppConfig config) {
		this.config = config;
	}

	List<Order> list() {
		return getDatabase().queryForList("select * from orders order by title", this::toOrder);
	}

	Order retrieve(int id) {
		return getDatabase().queryForSingle("select * from orders where id = ?", id,
                row -> toOrderWithOrderLines(id, row))
				.orElseThrow(notFound(getClass(), id));
	}

	private Supplier<RequestException> notFound(Class<?> clazz, int id) {
		String className = clazz.getName();
		return () -> new RequestException(404, "Can't find " + className + " with id " + id);
	}

	void insert(Order order) {
		validateOrder(order);

		getDatabase().doInTransaction(() -> {
			int orderId = getDatabase().insert("insert into orders (title) values (?) returning id", order.getTitle());
			order.setId(orderId);
			insertOrderLines(order.getId(), order);
		});
	}

	public void update(int orderId, Order order) {
		validateOrder(order);

		getDatabase().doInTransaction(() -> {
			updateOrder(orderId, order);
			deleteOrderLines(orderId);
			insertOrderLines(orderId, order);
		});
	}


	private List<OrderLine> queryForOrderLines(int orderId) {
		return getDatabase()
				.queryForList(
						"select * from order_lines INNER JOIN products on products.id = order_lines.product_id where order_id = ?",
						this::toOrderLine, orderId);
	}

	private void deleteOrderLines(int orderId) {
		getDatabase().executeOperation("delete from order_lines where order_id = ?", orderId);
	}

	private void updateOrder(int orderId, Order order) {
		getDatabase().executeOperation("update orders set title = ? where id = ?", order.getTitle(), orderId);
	}

	private void insertOrderLines(int orderId, Order order) {
		for (OrderLine orderLine : order.getOrderLines()) {
			getDatabase().executeOperation("insert into order_lines (amount, product_id, title, order_id) values (?, ?, ?, ?)",
					orderLine.getAmount(), orderLine.getProductId(), orderLine.getTitle(), orderId);
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

    public Database getDatabase() {
        return config.getDatabase();
    }
}
