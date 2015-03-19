package net.openright.simpleserverseed.domain.orders;

import java.util.Objects;

class OrderLine {

	private String title;

	OrderLine(String title) {
		this.title = title;
	}

	String getTitle() {
		return title;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof OrderLine) {
			OrderLine other = (OrderLine) obj;
			return Objects.equals(title, other.title);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(title);
	}

	@Override
	public String toString() {
		return "OrderLine {title = " + Objects.toString(title, "no title set") + "}";
	}
}
