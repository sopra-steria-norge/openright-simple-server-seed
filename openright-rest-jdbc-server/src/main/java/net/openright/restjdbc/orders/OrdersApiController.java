package net.openright.restjdbc.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import net.openright.infrastructure.rest.JsonController;

import org.json.JSONArray;
import org.json.JSONObject;

public class OrdersApiController implements JsonController {

	private List<Order> orders = new ArrayList<>();

	public OrdersApiController() {
		orders.add(new Order(4211, "My order"));
		orders.add(new Order(123, "Your order"));
	}

	@Override
	public JSONObject getJSON(HttpServletRequest req) {
		return new JSONObject()
			.put("orders", collect(orders.stream().map(this::toJSON)));
	}
	
	@Override
	public void postJSON(JSONObject jsonObject) {
		orders.add(new Order(jsonObject.getInt("id"), jsonObject.getString("title")));
	}

	private JSONArray collect(Stream<JSONObject> stream) {
		return new JSONArray(stream.collect(Collectors.toList()));
	}

	private JSONObject toJSON(Order order) {
		return new JSONObject().put("id", order.getId()).put("title", order.getTitle());
	}

}
