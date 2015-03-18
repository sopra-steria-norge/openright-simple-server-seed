package net.openright.simpleserverseed.domain.orders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.db.PgsqlDatabase.DatabaseTable;
import net.openright.infrastructure.rest.RequestException;

public class OrdersRepository {

	private DatabaseTable table;
	private PgsqlDatabase database;
	private DatabaseTable lineTable;

	public OrdersRepository(PgsqlDatabase database) {
		this.database = database;
		this.table = database.table("orders");
		this.lineTable = database.table("order_lines");
	}

	public List<Order> list() {
		return table.list(this::toOrder);
	}

	private Order toOrder(ResultSet rs) throws SQLException {
		Order order = new Order(rs.getString("title"));
		order.setId(rs.getInt("id"));
		return order;
	}

	private OrderLine toOrderLine(ResultSet rs) throws SQLException {
		return new OrderLine(rs.getString("title"));
	}

	public void insert(Order order) {
		if (order.getTitle().equals("null")) {
			throw new RuntimeException("Null title is invalid");
		}
		if (order.getTitle().contains("foul")) {
			throw new RequestException("No foul language in orders, please!");
		}


		database.doInTransaction(() -> {
			order.setId(table.insertValues(row -> {
				row.put("title", order.getTitle());
			}));

			for (OrderLine orderLine : order.getOrderLines()) {
				lineTable.insertValues(row -> {
					row.put("order_id", order.getId());
					row.put("title", orderLine.getTitle());
				});
			}
		});
	}

	public Order retrieve(int id) {
		Order order = table.where("id", id).single(this::toOrder);
		order.setOrderLines(
				lineTable.where("order_id", id).list(this::toOrderLine));
		return order;
	}
}
