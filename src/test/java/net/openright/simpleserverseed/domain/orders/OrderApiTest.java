package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;
import net.openright.infrastructure.test.SampleData;
import net.openright.simpleserverseed.domain.products.Product;
import net.openright.simpleserverseed.domain.products.ProductRepositoryTest;

import org.junit.Test;

public class OrderApiTest {

    private OrdersApiController controller = new OrdersApiController(null);

    @Test
    public void shouldTransformEmptyOrder() throws Exception {
        Order order = OrderRepositoryTest.sampleOrder();
        assertThat(controller.toOrder(controller.toJSON(order))).isEqualTo(order);
    }

    @Test
    public void shouldTransformOrders() throws Exception {
        Order order = sampleOrder();
        Order transformed = controller.toOrder(controller.toJSON(order));
        assertThat(transformed).isEqualTo(order);
        assertThat(transformed.toString()).isEqualTo(order.toString());
        assertThat(transformed.hashCode()).isEqualTo(order.hashCode());
    }

    private Order sampleOrder() {
        Order order = OrderRepositoryTest.sampleOrderWithLines(sampleProduct(), sampleProduct());
        return order;
    }

    private Product sampleProduct(){
        Product product = ProductRepositoryTest.sampleProduct();
        product.setId(SampleData.randomId());
        return product;
    }

}
