package net.openright.simpleserverseed.domain.orders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.openright.simpleserverseed.infrastructure.db.PgsqlDatabase;
import net.openright.simpleserverseed.infrastructure.db.PgsqlDatabase.DatabaseTable;
import net.openright.simpleserverseed.infrastructure.rest.RequestException;

public class OrdersRepository {

	private DatabaseTable table;

	public OrdersRepository(PgsqlDatabase database) {
		this.table = database.table("orders");
	}

	public List<Order> list() {
		return table.list(this::toOrder);
	}

	private Order toOrder(ResultSet rs) throws SQLException {
		Order order = new Order(rs.getString("title"), new ArrayList<OrderLine>());
		order.setId(rs.getInt("id"));
		return order;
	}

	public void insert(Order order) {
		if (order.getTitle().equals("null")) {
			throw new RuntimeException("Null title is invalid");
		}
		if (order.getTitle().contains("foul")) {
			throw new RequestException("No foul language in orders, please!");
		}

		order.setId(table.insertValues(row -> {
			row.put("title", order.getTitle());
		}));
	}

	public Order retrieve(int id) {
		return table.where("id", id).single(this::toOrder);
	}
}
