package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.rest.ResourceApi;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.domain.couponValidator.CoupongValidatorGateway;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrdersApiController implements ResourceApi {

    private OrdersRepository repository;
    private CoupongValidatorGateway coupongValidatorGateway;

    public OrdersApiController(SeedAppConfig config, CoupongValidatorGateway coupongValidatorGateway) {
        this.repository = new OrdersRepository(config);
        this.coupongValidatorGateway = coupongValidatorGateway;
    }

    @Override
    public JSONObject getResource(String id) {
        return toJSON(repository.retrieve(Long.parseLong(id)));
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
        double price = calculatePrice(order, jsonObject.getString("coupon"));
        repository.insertOrderPrice(order.getId(), price);
        return String.valueOf(order.getId());
    }

    private double calculatePrice(Order order, String coupon) {
        double price = getPrice(order);
        if(coupon == null || coupon.isEmpty()){
            return price;
        }

        if(coupongValidatorGateway.validate(coupon)){
            return price * 0.5;
        }

        throw new RequestException("Invalid code");
    }

    private double getPrice(Order order) {
        return repository.retrieve(order.getId()).getOrderLines().stream()
                .map(OrderLine::getPrice)
                .reduce(0.0, (a, b) -> a+b);
    }

    @Override
    public void updateResource(String id, JSONObject jsonObject) {
        repository.update(Long.parseLong(id), toOrder(jsonObject));
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
