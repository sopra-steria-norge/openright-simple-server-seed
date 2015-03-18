package net.openright.simpleserverseed.domain.orders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import net.openright.simpleserverseed.infrastructure.db.Database;
import net.openright.simpleserverseed.infrastructure.db.Database.DatabaseTable;
import net.openright.simpleserverseed.infrastructure.rest.JsonController;
import net.openright.simpleserverseed.infrastructure.rest.RequestException;

import org.json.JSONArray;
import org.json.JSONObject;

public class OrdersApiController implements JsonController {

	private DatabaseTable table;
	private OrderLineDAO orderLineDAO;

	public OrdersApiController(Database database) {
		this.table = database.table("orders");
		orderLineDAO = new OrderLineDAO(database);
	}

	@Override
	public JSONObject getJSON(HttpServletRequest req) {
		return new JSONObject().put("orders",
				collect(getOrders().stream().map(this::toJSON)));
	}

	List<Order> getOrders() {
		List<Order> orders = table.list(rs -> new Order(rs.getInt("id"), rs
				.getString("title"), orderLineDAO.getOrderLines(rs.getInt("id"))));

		return orders;
	}

	Order getOrder(int id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", id + "");
		Order order = table.list(
				rs -> new Order(rs.getInt("id"), rs.getString("title"), orderLineDAO.getOrderLines(rs.getInt("id"))), map)
				.get(0);
		return order;
	}

	@Override
	public void postJSON(JSONObject jsonObject) {
		postOrder(toOrder(jsonObject));
	}

	void postOrder(Order order) {
		if (order.getTitle().equals("null")) {
			throw new RuntimeException("Null title is invalid");
		}
		if (order.getTitle().contains("foul")) {
			throw new RequestException("No foul language in orders, please!");
		}

		table.insertValues(row -> {
			row.put("id", order.getId());
			row.put("title", order.getTitle());
			orderLineDAO.postOrderLines(order.getId(), order.getOrderLines());
		});
	}

	private Order toOrder(JSONObject jsonObject) {
		return new Order(jsonObject.getInt("id"), jsonObject.getString("title"), null);
	}

	private JSONArray collect(Stream<JSONObject> stream) {
		return new JSONArray(stream.collect(Collectors.toList()));
	}

	JSONObject toJSON(Order order) {
		return new JSONObject().put("id", order.getId()).put("title",
				order.getTitle());
	}

}
