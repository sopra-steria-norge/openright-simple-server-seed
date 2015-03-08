package net.openright.restjdbc.orders;

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

	private DatabaseTable table;

	public OrdersApiController(Database database) {
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

	void postOrder(Order order) {
		table.insertValues(row -> {
			row.put("id", order.getId());
			row.put("title", order.getTitle());
		});
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
