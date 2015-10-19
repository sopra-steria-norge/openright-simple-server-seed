package net.openright.simpleserverseed.domain.products;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.JsonTextValue;

import net.openright.infrastructure.rest.ResourceApi;
import net.openright.simpleserverseed.application.SeedAppConfig;

public class ProductsApiController implements ResourceApi {

    private ProductRepository repository;

    public ProductsApiController(SeedAppConfig config) {
        this.repository = new ProductRepository(config);
    }

    @Override
    public JsonObject listResources() {
        return new JsonObject()
            .withValue("products",
                    JsonArray.fromStream(repository.list().stream().map(this::toJSON)));
    }

    @Override
    public JsonObject getResource(String id) {
        return toJSON(repository.retrieve(Long.valueOf(id)));
    }

    @Override
    public String createResource(JsonObject JsonObject) {
        Product product = toProduct(JsonObject);
        repository.insert(product);
        return String.valueOf(product.getId());
    }

    @Override
    public void updateResource(String id, JsonObject JsonObject) {
        repository.update(Long.valueOf(id), toProduct(JsonObject));
    }

    private JsonObject toJSON(Product product) {
        return new JsonObject()
            .withValue("id", product.getId())
            .withValue("title", product.getTitle())
            .withValue("price", product.getPrice())
            .withValue("description", product.getDescription());
    }

    private Product toProduct(JsonObject jsonObject) {
        Product product = new Product();
        product.setTitle(jsonObject.requiredString("title"));
        product.setPrice(requiredDouble(jsonObject, "price"));
        product.setDescription(jsonObject.requiredString("description"));
        return product;
    }

    private double requiredDouble(JsonObject jsonObject, String string) {
        return jsonObject.value(string)
                .map(n -> Double.parseDouble(((JsonTextValue)n).textValue()))
                .get();
    }

}
