package net.openright.simpleserverseed.domain.products;

import org.jsonbuddy.JsonObject;

import java.util.Objects;

public class Product {

    private Long id;
    private String title;
    private String description;
    private boolean active = true;
    private double price;

    static Product fromJson(JsonObject jsonObject) {
        Product product = new Product();
        product.setTitle(jsonObject.requiredString("title"));
        product.setPrice(jsonObject.requiredDouble("price"));
        product.setDescription(jsonObject.requiredString("description"));
        return product;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setInactive() {
        this.active = false;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ",title=" + title + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Product)) return false;
        Product that = (Product)obj;
        return Objects.equals(this.id, that.id) && Objects.equals(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }


    JsonObject toJson() {
        return new JsonObject()
            .put("id", getId())
            .put("title", getTitle())
            .put("price", getPrice())
            .put("description", getDescription());
    }
}
