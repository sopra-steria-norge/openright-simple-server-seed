package net.openright.simpleserverseed.domain.orders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.openright.simpleserverseed.infrastructure.db.Database;
import net.openright.simpleserverseed.infrastructure.db.Database.DatabaseTable;

public class OrderLineDAO {
	
	private DatabaseTable table;

	public OrderLineDAO(Database database) {
		this.table = database.table("orderlines");
	}
	
	List<OrderLine> getOrderLines(int orderId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("orderid", orderId + "");
		List<OrderLine> orderLines = table.list(
				rs -> new OrderLine(rs.getInt("id"), rs.getString("title")), map);
		return orderLines;
	}

	public void postOrderLines(int orderId, List<OrderLine> orderLines) {
		
		orderLines.stream().forEach(s -> {
			table.insertValues(row -> {
				row.put("id", s.getId());
				row.put("orderId", orderId);
				row.put("title", s.getTitle());
			});
		});
		
	}
}
