package net.openright.simpleserverseed.domain.orders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.db.PgsqlDatabase.Row;
import net.openright.infrastructure.rest.RequestException;
import net.openright.simpleserverseed.domain.products.ProductRepository;

class OrdersRepository {

	private PgsqlDatabase database;

	OrdersRepository(PgsqlDatabase database) {
		this.database = database;
	}

	List<Order> list() {
		String query = "select * from orders order by title";

		return database.executeDbOperation(query, new Object[] { }, stmt -> {
			try (ResultSet rs = stmt.executeQuery()) {
				List<Order> result = new ArrayList<Order>();
				while (rs.next()) {
					Order order = toOrder(new Row(rs));
					result.add(order);
					
				}
				return result;
			}
		});
	}

	Order retrieve(int id) {
		String query = "select * from orders where id = ?";
		Object[] parameters = new Object[] { id };

		return database.executeDbOperation(query, parameters, stmt -> {
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					throw new RequestException(404, "Order " + id + " not found");
				}
				Order result = toOrder(new Row(rs));
				result.setOrderLines(retrieveOrderLines(id));
				if (rs.next()) {
					throw new RuntimeException("Duplicate");
				}
				return result;
			}
		});
	}

	List<OrderLine> retrieveOrderLines(int orderId) {
		String query = "select * from order_lines INNER JOIN products on products.id = order_lines.product_id where order_id = ?";
		Object[] parameters = new Object[] { orderId };

		return database.executeDbOperation(query, parameters, stmt -> {
			List<OrderLine> lines = new ArrayList<OrderLine>();
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lines.add(toOrderLine(new Row(rs)));
				}
			}
			return lines;
		});
	}

	void insert(Order order) {
		validateOrder(order);

		String query = "insert into orders (title) values (?) returning id";
		Object[] parameters = new Object[] { order.getTitle() };

		database.doInTransaction(() -> {
			order.setId(database.executeDbOperation(query, parameters, stmt -> {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				return rs.getInt("id");
			}));
			insertOrderLines(order.getId(), order);
		});
	}

	public void update(int orderId, Order order) {
		validateOrder(order);
		
		String query = "update orders set title = ? where id = ?";
		Object[] parameters = new Object[] { order.getTitle(), orderId };

		database.doInTransaction(() -> {
			
			database.executeDbOperation(query, parameters, stmt -> {
				stmt.executeUpdate();
				return null;
			});
			
			deleteOrderLines(orderId);
			insertOrderLines(orderId, order);
		});
	}

	private void deleteOrderLines(int orderId) {
		String query = "delete from order_lines where order_id = ?";
		Object[] parameters = new Object[] { orderId };
		
		database.executeDbOperation(query, parameters, stmt -> {
			stmt.execute();
			return null;
		});
	}

	private void insertOrderLines(int orderId, Order order) {
		for (OrderLine orderLine : order.getOrderLines()) {

			String query = "insert into order_lines (amount, product_id, title, order_id) values (?, ?, ?, ?) returning id";
			Object[] parameters = new Object[] { orderLine.getAmount(), orderLine.getProductId(), orderLine.getTitle(), orderId };

			database.executeDbOperation(query, parameters, stmt -> {
				stmt.executeQuery();
				return null;
			});
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

	private Order toOrder(Row row) throws SQLException {
		Order order = new Order(row.getString("title"));
		order.setId(row.getInt("id"));
		return order;
	}

	private OrderLine toOrderLine(Row row) throws SQLException {
		OrderLine orderLine = new OrderLine(row.getString("title"));
		orderLine.setProduct(ProductRepository.toProduct(row));
		orderLine.setAmount(row.getInt("amount"));
		return orderLine;
	}
}
