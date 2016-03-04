package net.openright.simpleserverseed.domain.orders;

import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;
import net.openright.simpleserverseed.domain.products.Product;
import net.openright.simpleserverseed.domain.products.ProductRepository;
import net.openright.simpleserverseed.domain.products.ProductRepositoryTest;
import org.assertj.core.api.AssertionsForClassTypes;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class OrdersApiControllerTest {

    private SeedAppConfig config = SimpleseedTestConfig.instance();
    private final OrdersApiController controller = new OrdersApiController(config);

    private final ProductRepository productRepository = new ProductRepository(config);
    private final Product product1 = ProductRepositoryTest.sampleProduct();
    private final Product product2 = ProductRepositoryTest.sampleProduct();

    @Test
    public void shouldListInsertedResources() {
        String orderTitle = "Some title";
        JsonObject jsonObject = new JsonObject()
                .put("title", orderTitle)
                .put("orderlines", new JsonArray()
                        .add(new JsonObject().put("amount", 100).put("productId", product1.getId()))
                        .add(new JsonObject().put("amount", 2).put("productId", product2.getId()))
                );
        String id = controller.createResource(jsonObject);
        jsonObject.put("id", Integer.parseInt(id));

        jsonObject.put("orderlines", new JsonArray());
        assertThat(controller.listResources().requiredArray("orders"))
                .contains(jsonObject);
    }

    @Test
    public void shouldRetrieveUpdatedValues() {
        JsonObject jsonObject = new JsonObject()
                .put("title", "old title")
                .put("orderlines", new JsonArray()
                        .add(new JsonObject().put("amount", 100).put("productId", product1.getId()))
                        .add(new JsonObject().put("amount", 2).put("productId", product2.getId()))
                );
        String id = controller.createResource(jsonObject);
        jsonObject.put("id", Integer.parseInt(id));
        jsonObject.put("title", "Updated title");

        controller.updateResource(id, jsonObject);
        AssertionsForClassTypes.assertThat(controller.getResource(id))
                .isEqualTo(jsonObject);
    }


        @Before
    public void saveProducts() {
        productRepository.insert(product1);
        productRepository.insert(product2);
    }

    @Test
    public void shouldUpdateOrder() {
        String orderTitle = "Some title";
        JsonObject jsonObject = new JsonObject()
                .put("title", orderTitle)
                .put("orderlines", new JsonArray()
                        .add(new JsonObject().put("amount", 100).put("productId", product1.getId()))
                );
        String id = controller.createResource(jsonObject);

        jsonObject.put("id", Integer.parseInt(id));
        jsonObject.requiredArray("orderlines")
                .add(new JsonObject().put("amount", 10).put("productId", product2.getId()));
        controller.updateResource(id, jsonObject);

        assertThat(controller.getResource(id))
                .isEqualTo(jsonObject);
    }

}