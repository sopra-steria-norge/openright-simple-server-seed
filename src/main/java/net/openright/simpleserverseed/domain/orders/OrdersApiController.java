package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.rest.ResourceApi;
import net.openright.simpleserverseed.application.SeedAppConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrdersApiController implements ResourceApi {

    private OrdersRepository repository;

    public OrdersApiController(SeedAppConfig config) {
        this.repository = new OrdersRepository(config);
    }

    @Override
    public JSONObject getResource(String id) {
        return toJSON(repository.retrieve(Integer.parseInt(id)));
    }

    @Override
    public JSONObject listResources() {
        return new JSONObject()
            .put("orders", mapToJSON(repository.list(), this::toJSON));
    }

    @Override
    public String createResource(JSONObject jsonObject) {
        Order order = toOrder(jsonObject);
        repository.insert(order);
        return String.valueOf(order.getId());
    }

    @Override
    public void updateResource(String id, JSONObject jsonObject) {
        repository.update(Integer.parseInt(id), toOrder(jsonObject));
    }

    private Order toOrder(JSONObject jsonObject) {
        Order order = new Order(jsonObject.getString("title"));

        JSONArray jsonArray = jsonObject.getJSONArray("orderlines");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject orderLine = jsonArray.getJSONObject(i);
            if (orderLine.getString("amount").isEmpty()) {
                continue;
            }
            order.addOrderLine(orderLine.optLong("product"), orderLine.optInt("amount"));
        }

        return order;
    }

    private JSONObject toJSON(Order order) {
        return new JSONObject()
            .put("id", order.getId())
            .put("title", order.getTitle())
            .put("orderlines", mapToJSON(order.getOrderLines(), this::toJSON));
    }

    private JSONObject toJSON(OrderLine line) {
        return new JSONObject()
            .put("productId", line.getProductId())
            .put("amount", line.getAmount());
    }

    private <T> JSONArray mapToJSON(List<T> list, Function<T, JSONObject> mapper) {
        return new JSONArray(list.stream().map(mapper).collect(Collectors.toList()));
    }
}
