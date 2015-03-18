package net.openright.simpleserverseed.domain.orders;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.rest.JsonController;

import org.json.JSONArray;
import org.json.JSONObject;

public class OrdersApiController implements JsonController {

	private OrdersRepository repository;

	public OrdersApiController(PgsqlDatabase database) {
		this.repository = new OrdersRepository(database);
	}

	@Override
	public JSONObject getJSON(HttpServletRequest req) {
		return new JSONObject()
			.put("orders", collect(repository.list().stream().map(this::toJSON)));
	}

	@Override
	public void postJSON(JSONObject jsonObject) {
		repository.insert(toOrder(jsonObject));
	}

	private Order toOrder(JSONObject jsonObject) {
		Order order = new Order(jsonObject.getString("title"));
		order.setId(jsonObject.getInt("id"));
		return order;
	}

	private JSONArray collect(Stream<JSONObject> stream) {
		return new JSONArray(stream.collect(Collectors.toList()));
	}

	JSONObject toJSON(Order order) {
		return new JSONObject().put("id", order.getId()).put("title", order.getTitle());
	}

}
