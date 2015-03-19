package net.openright.simpleserverseed.domain.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Order {

	private String title;
	private Integer id;
	private List<OrderLine> orderLines = new ArrayList<>();

	Order(String title) {
		this.title = title;
	}

	void addOrderLine(String title) {
		this.orderLines.add(new OrderLine(title));
	}
	
	void setOrderLines(List<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

	void setId(int id) {
		this.id = id;
	}

	int getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	List<OrderLine> getOrderLines() {
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
