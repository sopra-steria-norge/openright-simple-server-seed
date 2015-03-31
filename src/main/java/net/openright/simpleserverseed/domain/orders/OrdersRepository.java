package net.openright.simpleserverseed.domain.orders;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.db.PgsqlDatabase.DatabaseTable;
import net.openright.infrastructure.db.PgsqlDatabase.Row;
import net.openright.infrastructure.rest.RequestException;
import net.openright.simpleserverseed.domain.products.ProductRepository;

class OrdersRepository {

    private DatabaseTable table;
    private PgsqlDatabase database;
    private DatabaseTable lineTable;

    OrdersRepository(PgsqlDatabase database) {
        this.database = database;
        this.table = database.table("orders");
        this.lineTable = database.table("order_lines");
    }

    List<Order> list() {
        return table.list(this::toOrder);
    }

    Order retrieve(int id) {
        Order order = table.where("id", id).single(this::toOrder);
        if (order == null) {
            throw new RequestException(404, "Order " + id + " not found");
        }
        order.setOrderLines(lineTable
                .where("order_id", id)
                .join("products", "product_id", "id")
                .list(this::toOrderLine));
        return order;
    }

    void insert(Order order) {
        validateOrder(order);

        database.doInTransaction(() -> {
            order.setId(table.insertValues(row -> toRow(order, row)));
            insertOrderLines(order.getId(), order);
        });
    }

    public void update(int orderId, Order order) {
        validateOrder(order);

        database.doInTransaction(() -> {
            table.where("id", orderId).updateValues(row -> toRow(order, row));
            lineTable.where("order_id", orderId).delete();
            insertOrderLines(orderId, order);
        });
    }

    private void insertOrderLines(int orderId, Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            lineTable.insertValues(row -> toRow(orderId, orderLine, row));
        }
    }

    private Object toRow(Order order, Map<String, Object> row) {
        return row.put("title", order.getTitle());
    }

    private void toRow(int orderId, OrderLine orderLine, Map<String, Object> row) {
        row.put("order_id", orderId);
        row.put("title", orderLine.getTitle());
        row.put("product_id", orderLine.getProductId());
        row.put("amount", orderLine.getAmount());
    }

    private void validateOrder(Order order) {
        if (order.getTitle().equals("null")) {
            throw new RuntimeException("Null title is invalid");
        }
        if (order.getTitle().contains("foul")) {
            throw new RequestException("No foul language in orders, please!");
        }
    }

    private Order toOrder(Row row) throws SQLException {
        Order order = new Order(row.getString("title"));
        order.setId(row.getInt("id"));
        return order;
    }

    private OrderLine toOrderLine(Row row) throws SQLException {
        OrderLine orderLine = new OrderLine(row.getString("title"));
        orderLine.setProduct(ProductRepository.toProduct(row));
        orderLine.setAmount(row.getInt("amount"));
        return orderLine;
    }
}
