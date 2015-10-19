package net.openright.simpleserverseed.domain.orders;

import java.util.Objects;
import java.util.Optional;

import net.openright.simpleserverseed.domain.products.Product;

class OrderLine {

    private String title;
    private Long productId;
    private long amount;
    private Optional<Product> product = Optional.empty();

    OrderLine(String title) {
        this.title = title;
    }

    public OrderLine(Long productId, long amount) {
        this(amount + " of product " + productId);
        this.productId = productId;
        this.amount = amount;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    Long getProductId() {
        return productId;
    }

    long getAmount() {
        return amount;
    }

    void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderLine)) {
            return false;
        }
        OrderLine that = (OrderLine) obj;
        return Objects.equals(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "OrderLine {title = " + Objects.toString(title, "no title set") + "}";
    }

    public Optional<Product> getProduct() {
        return product;
    }

    void setProduct(Product product) {
        this.product = Optional.of(product);
        this.productId = product.getId();
    }

    public double getPrice() {
        return this.product.get().getPrice() * amount;
    }
}
