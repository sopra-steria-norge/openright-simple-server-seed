package net.openright.simpleserverseed.domain.orders;

import java.util.Objects;

public class OrderLine {
	
	private int id;
	private String title;
	
	public OrderLine(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof OrderLine) {
			OrderLine other = (OrderLine) obj;
			return Objects.equals(id, other.id)
					&& Objects.equals(title, other.title);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title);
	}

	@Override
	public String toString() {
		return "OrderLine {id = " + Objects.toString(id) + ", title = "
				+ Objects.toString(title, "no title set") + "}";
	}
}
