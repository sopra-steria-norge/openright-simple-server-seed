package net.openright.simpleserverseed.domain.products;

import net.openright.infrastructure.test.SampleData;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductRepositoryTest {

    private SeedAppConfig config = SimpleseedTestConfig.instance();
    private ProductRepository repository = new ProductRepository(config);

    @Test
    public void shouldRetrieveSavedProduct() throws Exception {
        Product product = sampleProduct();
        repository.insert(product);

        assertThat(repository.retrieve(product.getId()))
            .isEqualToComparingFieldByField(product);
    }

    @Test
    public void shouldUpdateProduct() throws Exception {
        Product product = sampleProduct();
        repository.insert(product);

        product.setTitle("New title");
        repository.update(product.getId(), product);

        assertThat(repository.retrieve(product.getId()).getTitle())
            .isEqualTo("New title");
    }

    @Test
    public void shouldListActiveProducts() throws Exception {
        Product product1 = sampleProduct("z");
        Product product2 = sampleProduct("a");
        Product inactiveProduct = sampleProduct();
        inactiveProduct.setInactive();

        repository.insert(product1);
        repository.insert(product2);
        repository.insert(inactiveProduct);

        assertThat(repository.list())
            .containsSubsequence(product2, product1)
            .doesNotContain(inactiveProduct);
    }

    private static Product sampleProduct(String prefix) {
        Product product = new Product();
        product.setTitle(prefix + SampleData.sampleString(3));
        product.setDescription(SampleData.sampleString(10));
        product.setPrice(SampleData.randomAmount());
        return product;
    }

    public static Product sampleProduct() {
        return sampleProduct("");
    }

}
