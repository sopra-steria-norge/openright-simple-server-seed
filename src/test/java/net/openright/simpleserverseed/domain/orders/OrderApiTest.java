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
        order.setId(SampleData.randomId());
        assertThat(controller.toOrder(controller.toJSON(order))).isEqualTo(order);
    }

    @Test
    public void shouldTransformOrders() throws Exception {
        Order order = sampleOrder();
        assertThat(controller.toOrder(controller.toJSON(order))).isEqualTo(order);
    }

    private Order sampleOrder() {
        Order order = OrderRepositoryTest.sampleOrderWithLines(sampleProduct(), sampleProduct());
        order.setId(SampleData.randomId());
        return order;
    }

    private Product sampleProduct(){
        Product product = ProductRepositoryTest.sampleProduct();
        product.setId(SampleData.randomId());
        return product;
    }

}
