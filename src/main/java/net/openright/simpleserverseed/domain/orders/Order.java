package net.openright.simpleserverseed.domain.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Order {

    private String title;
    private Long id;
    private List<OrderLine> orderLines = new ArrayList<>();
    private Double price;

    Order(String title) {
        this.title = title;
    }

    public void addOrderLine(Long productId, int amount) {
        this.orderLines.add(new OrderLine(productId, amount));
    }

    public Double getTotalAmount() {
        return price;
    }

    Order withOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
        return this;
    }

    Order withPrice(Double price) {
        this.price = price;
        return this;
    }

    void setId(Long id) {
        this.id = id;
    }

    Long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    List<OrderLine> getOrderLines() {
        return orderLines;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Order)) {
            return false;
        }

        Order other = (Order) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(title, other.title) && Objects.equals(orderLines, other.orderLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "Order {id = " + id
                + ", title = " + title
                + "," + orderLines.toString()
                + "}";
    }
}
