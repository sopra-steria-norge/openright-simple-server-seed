package net.openright.simpleserverseed.domain.orders;

import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.test.SampleData;
import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.simpleserverseed.application.InMemTestClass;
import net.openright.simpleserverseed.domain.couponValidator.CoupongValidatorGateway;
import net.openright.simpleserverseed.domain.products.ProductsApiController;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.fail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class OprettOrderApiControllerTest extends InMemTestClass {

    private OrdersApiController orderController = new OrdersApiController(config, new CoupongValidatorGateway());
    private OrdersRepository ordersRepository = new OrdersRepository(config);
    private ProductsApiController productsApiController = new ProductsApiController(config);

    @Test
    public void validCouponReducePrice() throws Exception {
        int amount = 4;
        String coupon = "hei";
        double priceReduction = 0.5;
        CoupongValidatorGateway mockCoupongValidatorGateway = mockExternalDependency();
        when(mockCoupongValidatorGateway.validate(anyString())).thenReturn(true);
        JSONObject sampleProduct = sampleProduct();
        String productId = storeProduct(sampleProduct);
        JSONObject sampleOrder = sampleOrder(coupon, productId, "" + amount);

        String orderId = createResourceInTransaction(sampleOrder);

        Order actual = ordersRepository.retrieve(Long.parseLong(orderId));
        assertOrdersAreEqual(actual, sampleOrder);
        assertThat(actual.getTotalAmount()).isEqualTo(sampleProduct.getDouble("price") * amount * priceReduction);
        verify(mockCoupongValidatorGateway, times(1)).validate(coupon);
    }

    @Test
    public void noChangeIfInvalidCoupon() throws Exception {
        int amount = 4;
        String coupon = "hei";
        CoupongValidatorGateway mockCoupongValidatorGateway = mockExternalDependency();
        when(mockCoupongValidatorGateway.validate(anyString())).thenReturn(false);
        JSONObject sampleProduct = sampleProduct();
        String productId = storeProduct(sampleProduct);
        JSONObject sampleOrder = sampleOrder(coupon, productId, "" + amount);

        try {
            createResourceInTransaction(sampleOrder);
            fail();
        } catch (RequestException e){
            assertThat(e.getMessage()).isEqualTo("Invalid code");
        }
        assertThat(ordersRepository.list()).isEmpty();
        verify(mockCoupongValidatorGateway, times(1)).validate(coupon);
    }

    private String createResourceInTransaction(JSONObject sampleOrder) {
        final String[] resourceId = new String[1];
        config.getDatabase().doInTransaction(() -> {
            try {
                resourceId[0] = orderController.createResource(sampleOrder);
            } catch (Exception e) {
                throw ExceptionUtil.soften(e);
            }
        });
        return resourceId[0];
    }

    private void assertOrdersAreEqual(Order order, JSONObject jsonProduct) {
        Order fromJson = orderController.toOrder(jsonProduct);

        assertThat(order).isNotNull();
        assertThat(order.getTitle()).isEqualTo(fromJson.getTitle());
        assertThat(order.getOrderLines()).isEqualTo(fromJson.getOrderLines());
    }

    private String storeProduct(JSONObject sampleProduct) {
        return productsApiController.createResource(sampleProduct);
    }

    private CoupongValidatorGateway mockExternalDependency() {
        CoupongValidatorGateway mockCoupongValidatorGateway = Mockito.mock(CoupongValidatorGateway.class);
        orderController = new OrdersApiController(config, mockCoupongValidatorGateway);
        return mockCoupongValidatorGateway;
    }

    private JSONObject sampleOrder(String coupon, String productId, String amount){
        return new JSONObject()
                .put("coupon", coupon)
                .put("id", "")
                .put("title", SampleData.sampleString(2))
                .put("orderlines", new JSONArray()
                        .put(new JSONObject()
                                .put("product", productId)
                                .put("amount", amount)
                        )
                );
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
