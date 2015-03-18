package net.openright.simpleserverseed.domain.orders;

import java.util.List;
import java.util.Objects;

public class Order {

	private String title;
	private Integer id;
	private List<OrderLine> orderLines;

	public Order(String title, List<OrderLine> orderLines) {
		this.title = title;
		this.orderLines = orderLines;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public List<OrderLine> getOrderLines() {
		return orderLines;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Order) {
			Order other = (Order) obj;
			return Objects.equals(id, other.id)
					&& Objects.equals(title, other.title) && Objects.equals(orderLines, other.orderLines);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title);
	}

	@Override
	public String toString() {
		return "Order {id = " + Objects.toString(id) + ", title = "
				+ Objects.toString(title, "no title set") +
				orderLines.toString() +	"}";
	}
}
