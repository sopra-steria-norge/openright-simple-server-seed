package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.rest.ResourceApi;
import net.openright.simpleserverseed.application.SeedAppConfig;

import java.util.List;
import java.util.stream.Collectors;

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
            .put("orders", JsonArray.map(repository.list(), this::toJSON));
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
        return new Order(jsonObject.requiredString("title"))
                .withOrderLines(toOrderLines(jsonObject.requiredArray("orderlines")));
    }

    private List<OrderLine> toOrderLines(JsonArray orderLines) {
        return orderLines.objectStream()
            .filter(o -> o.longValue("amount").isPresent())
            .map(o -> new OrderLine(o.requiredLong("productId"), o.requiredLong("amount")))
            .collect(Collectors.toList());
    }

    private JsonObject toJSON(Order order) {
        return new JsonObject()
            .put("id", order.getId())
            .put("title", order.getTitle())
            .put("orderlines", JsonArray.map(order.getOrderLines(), this::toJSON));
    }

    private JsonObject toJSON(OrderLine line) {
        return new JsonObject()
            .put("productId", line.getProductId())
            .put("amount", line.getAmount());
    }
}
