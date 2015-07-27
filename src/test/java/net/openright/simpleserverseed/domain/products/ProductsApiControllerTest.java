package net.openright.simpleserverseed.domain.products;

import static org.assertj.core.api.Assertions.assertThat;
import net.openright.infrastructure.test.SampleData;

import org.junit.Test;

public class ProductsApiControllerTest {

    private Product product = ProductRepositoryTest.sampleProduct();

    @Test
    public void shouldTransformProduct() throws Exception {
        product.setId(SampleData.randomId());
        ProductsApiController controller = new ProductsApiController(null);
        assertThat(controller.toProduct(controller.toJSON(product))).isEqualTo(product);
    }

}
