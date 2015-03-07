package net.openright.restjdbc.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.db.Database.DatabaseTable;
import net.openright.infrastructure.rest.JsonController;

import org.json.JSONArray;
import org.json.JSONObject;

public class OrdersApiController implements JsonController {

	private List<Order> orders = new ArrayList<>();
	private DatabaseTable table;

	public OrdersApiController(Database database) {
		orders.add(new Order(4211, "My order"));
		orders.add(new Order(123, "Your order"));
		
		this.table = database.table("orders");
	}

	@Override
	public JSONObject getJSON(HttpServletRequest req) {
		return new JSONObject()
			.put("orders", collect(getOrders().stream().map(this::toJSON)));
	}

	List<Order> getOrders() {
		List<Order> orders =
		table.list(rs -> 
			new Order(rs.getInt("id"), rs.getString("title"))
		);
		
		return orders;
	}
	
	@Override
	public void postJSON(JSONObject jsonObject) {
		postOrder(toOrder(jsonObject));
	}

	boolean postOrder(Order order) {
		table.insertValues(row -> {
			row.put("id", order.getId());
			row.put("title", order.getTitle());
		});
		
		return orders.add(order);
	}

	private Order toOrder(JSONObject jsonObject) {
		return new Order(jsonObject.getInt("id"), jsonObject.getString("title"));
	}

	private JSONArray collect(Stream<JSONObject> stream) {
		return new JSONArray(stream.collect(Collectors.toList()));
	}

	JSONObject toJSON(Order order) {
		return new JSONObject().put("id", order.getId()).put("title", order.getTitle());
	}

}
