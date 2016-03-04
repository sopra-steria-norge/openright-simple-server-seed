package net.openright.simpleserverseed.domain.products;

import net.openright.infrastructure.test.SampleData;
import net.openright.simpleserverseed.application.InMemTestClass;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductApiControllerTest extends InMemTestClass {

    private ProductsApiController controller = new ProductsApiController(config);
    private ProductRepository repository = new ProductRepository(config);

    @Test
    public void shouldSaveProduct() throws Exception {
        JSONObject jsonObject = sampleProduct();

        String id = controller.createResource(jsonObject);

        assertProductsAreEqual(repository.retrieve(Long.parseLong(id)), jsonObject);
    }

    private void assertProductsAreEqual(Product product, JSONObject jsonProduct) {
        Product fromJson = controller.toProduct(jsonProduct);

        assertThat(product).isNotNull();
        assertThat(product.getTitle()).isEqualTo(fromJson.getTitle());
        assertThat(product.getPrice()).isEqualTo(fromJson.getPrice());
        assertThat(product.getDescription()).isEqualTo(fromJson.getDescription());
    }

    @Test
    public void shouldRetrieveSavedProduct() throws Exception {
        JSONObject jsonObject = sampleProduct();
        String id = controller.createResource(jsonObject);

        JSONObject fromController = controller.getResource(id);

        assertProductsAreEqual(repository.retrieve(Long.parseLong(id)), fromController);
    }

    @Test
    public void shouldUpdateProduct() throws Exception {
        JSONObject jsonObject = sampleProduct();
        String id = controller.createResource(jsonObject);
        jsonObject.put("title", "New title");

        controller.updateResource(id, jsonObject);

        assertProductsAreEqual(repository.retrieve(Long.parseLong(id)), jsonObject);
    }

    @Test
    public void shouldListActiveProducts() throws Exception {
        JSONObject a = sampleProduct("a");
        JSONObject b = sampleProduct("b");
        controller.createResource(a);
        controller.createResource(b);
        String c = controller.createResource(sampleProduct("c"));
        Product productC = repository.retrieve(Long.parseLong(c));
        productC.setActive(false);
        repository.update(productC.getId(), productC);

        JSONObject products = controller.listResources();

        List<Product> productsFromController = arrayToProducts(products);
        assertThat(productsFromController).containsOnly(controller.toProduct(a), controller.toProduct(b));
    }

    private List<Product> arrayToProducts(JSONObject actual) {
        JSONArray products = actual.getJSONArray("products");
        List<Product> productsFromController = new ArrayList<>();
        for (int i = 0; i < products.length(); i++) {
            productsFromController.add(controller.toProduct(products.getJSONObject(i)));
        }
        return productsFromController;
    }

    private static JSONObject sampleProduct() {
        return sampleProduct("");
    }

    private static JSONObject sampleProduct(String prefix) {
        return new JSONObject()
                .put("title", prefix + SampleData.sampleString(3))
                .put("price", SampleData.randomAmount())
                .put("description", SampleData.sampleString(10));
    }
}
