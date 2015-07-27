package net.openright.simpleserverseed.domain.products;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ProductsApiControllerTest {

    private Product product = ProductRepositoryTest.sampleProduct();
    private ProductsApiController controller = new ProductsApiController(null);

    @Test
    public void shouldTransformProduct() throws Exception {
        Product transformed = controller.toProduct(controller.toJSON(product));
        assertThat(transformed).isEqualTo(product);
        assertThat(transformed.toString()).isEqualTo(product.toString());
        assertThat(transformed.hashCode()).isEqualTo(product.hashCode());
    }

}
