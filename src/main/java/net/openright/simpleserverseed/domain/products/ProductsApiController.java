package net.openright.simpleserverseed.domain.products;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.rest.JsonController;

public class ProductsApiController implements JsonController {

    private ProductRepository repository;

    public ProductsApiController(PgsqlDatabase database) {
        this.repository = new ProductRepository(database);
    }

    @Override
    public JSONObject listJSON(HttpServletRequest req) {
        return new JSONObject()
            .put("products", mapToJSON(repository.list(), this::toJSON));
    }

    @Override
    public JSONObject getJSON(String id) {
        return toJSON(repository.retrieve(Long.valueOf(id)));
    }

    @Override
    public void postJSON(JSONObject jsonObject) {
        repository.insert(toProduct(jsonObject));
    }

    @Override
    public void putJSON(String id, JSONObject jsonObject) {
        repository.update(Long.valueOf(id), toProduct(jsonObject));
    }

    private <T> JSONArray mapToJSON(List<T> list, Function<T, JSONObject> mapper) {
        return new JSONArray(list.stream().map(mapper).collect(Collectors.toList()));
    }

    private JSONObject toJSON(Product product) {
        return new JSONObject()
            .put("id", product.getId())
            .put("title", product.getTitle())
            .put("price", product.getPrice())
            .put("description", product.getDescription());
    }

    private Product toProduct(JSONObject jsonObject) {
        Product product = new Product();
        product.setTitle(jsonObject.getString("title"));
        product.setPrice(jsonObject.getDouble("price"));
        product.setDescription(jsonObject.getString("description"));
        return product;
    }

}
