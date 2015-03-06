package net.openright.restjdbc.orders;

public class Order {

	private String title;
	private int id;

	public Order(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}

}
