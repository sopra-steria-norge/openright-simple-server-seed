package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.rest.ResourceApi;
import net.openright.simpleserverseed.application.SeedAppConfig;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;

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
            .withValue("orders",
                    JsonArray.fromStream(repository.list().stream().map(this::toJSON)));
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

        jsonObject.requiredArray("orderlines").nodeStream()
            .map(n -> (JsonObject)n)
            .filter(o -> o.longValue("amount") != null)
            .forEach(o -> order.addOrderLine(o.requiredLong("product"), o.requiredLong("amount")));

        return order;
    }

    private JsonObject toJSON(Order order) {
        return new JsonObject()
            .withValue("id", order.getId())
            .withValue("title", order.getTitle())
            .withValue("orderlines",
                    JsonArray.fromStream(order.getOrderLines().stream().map(this::toJSON)));
    }

    private JsonObject toJSON(OrderLine line) {
        return new JsonObject()
            .withValue("productId", line.getProductId())
            .withValue("amount", line.getAmount());
    }
}
