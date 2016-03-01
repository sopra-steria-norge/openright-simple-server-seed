package net.openright.simpleserverseed.domain.products;

import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;
import org.jsonbuddy.JsonObject;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ProductsApiControllerTest {
    private SeedAppConfig config = SimpleseedTestConfig.instance();
    private final ProductsApiController controller = new ProductsApiController(config);

    @Test
    public void shouldListInsertedProducts() {
        Product product = ProductRepositoryTest.sampleProduct();
        JsonObject productJson = new JsonObject()
                .put("title", product.getTitle())
                .put("description", product.getDescription())
                .put("price", product.getPrice());

        String id = controller.createResource(productJson);
        productJson.put("id", Integer.parseInt(id));

        assertThat(controller.listResources().requiredArray("products"))
                .contains(productJson);
    }

    @Test
    public void shouldRetrieveUpdatedValues() {
        Product product = ProductRepositoryTest.sampleProduct();
        JsonObject productJson = new JsonObject()
                .put("title", product.getTitle())
                .put("description", product.getDescription())
                .put("price", product.getPrice());

        String id = controller.createResource(productJson);
        productJson.put("id", Integer.parseInt(id));
        productJson.put("title", "updated title");
        productJson.put("description", "updated desc");
        productJson.put("price", 66.25);

        controller.updateResource(id, productJson);
        assertThat(controller.getResource(id))
                .isEqualToComparingFieldByField(productJson);
    }
}