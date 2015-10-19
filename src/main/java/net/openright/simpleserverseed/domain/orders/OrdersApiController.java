package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.rest.ResourceApi;
import net.openright.simpleserverseed.application.SeedAppConfig;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;

import java.util.List;
import java.util.function.Function;

public class OrdersApiController implements ResourceApi {

    private OrdersRepository repository;

    public OrdersApiController(SeedAppConfig config) {
        this.repository = new OrdersRepository(config);
    }

    @Override
    public JsonObject getResource(String id) {
        return toJSON(repository.retrieve(Integer.parseInt(id)));
    }

    @Override
    public JsonObject listResources() {
        return new JsonObject()
            .withValue("orders", mapToJSON(repository.list(), this::toJSON));
    }

    @Override
    public String createResource(JsonObject jsonObject) {
        Order order = toOrder(jsonObject);
        repository.insert(order);
        return String.valueOf(order.getId());
    }

    @Override
    public void updateResource(String id, JsonObject jsonObject) {
        repository.update(Integer.parseInt(id), toOrder(jsonObject));
    }

    private Order toOrder(JsonObject jsonObject) {
        Order order = new Order(jsonObject.requiredString("title"));

        for (JsonNode orderLineNode : jsonObject.requiredArray("orderLines")) {
            JsonObject orderLine = (JsonObject)orderLineNode;
            if (orderLine.longValue("amount").isPresent()) {
                order.addOrderLine(orderLine.requiredLong("product"), orderLine.requiredLong("amount"));
            }
        }

        return order;
    }

    private JsonObject toJSON(Order order) {
        return new JsonObject()
            .withValue("id", order.getId())
            .withValue("title", order.getTitle())
            .withValue("orderlines", mapToJSON(order.getOrderLines(), this::toJSON));
    }

    private JsonObject toJSON(OrderLine line) {
        return new JsonObject()
            .withValue("productId", line.getProductId())
            .withValue("amount", line.getAmount());
    }

    private <T> JsonArray mapToJSON(List<T> list, Function<T, JsonNode> mapper) {
        return JsonArray.fromNodeSteam(list.stream().map(mapper));
    }
}
