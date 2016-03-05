package net.openright.simpleserverseed.domain.products;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
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
            .put("products", JsonArray.map(repository.list(), net.openright.simpleserverseed.domain.products.Product::toJson));
    }

    @Override
    public JsonObject getResource(String id) {
        return repository.retrieve(Long.valueOf(id)).toJson();
    }

    @Override
    public String createResource(JsonObject JsonObject) {
        Product product = Product.fromJson(JsonObject);
        repository.insert(product);
        return String.valueOf(product.getId());
    }

    @Override
    public void updateResource(String id, JsonObject JsonObject) {
        repository.update(Long.valueOf(id), Product.fromJson(JsonObject));
    }

}
