package net.openright.simpleserverseed.domain.orders;

import java.sql.SQLException;
import java.util.List;

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

    void insert(Order order) {
        if (order.getTitle().equals("null")) {
            throw new RuntimeException("Null title is invalid");
        }
        if (order.getTitle().contains("foul")) {
            throw new RequestException("No foul language in orders, please!");
        }


        database.doInTransaction(() -> {
            order.setId(table.insertValues(row -> {
                row.put("title", order.getTitle());
            }));

            for (OrderLine orderLine : order.getOrderLines()) {
                lineTable.insertValues(row -> {
                    row.put("order_id", order.getId());
                    row.put("title", orderLine.getTitle());
                    row.put("product_id", orderLine.getProductId());
                    row.put("amount", orderLine.getAmount());
                });
            }
        });
    }

    protected Order retrieve(int id) {
        Order order = table.where("id", id).single(this::toOrder);
        order.setOrderLines(lineTable
                .where("order_id", id)
                .join("products", "product_id", "id")
                .list(this::toOrderLine));
        return order;
    }

    private Order toOrder(Row rs) throws SQLException {
        Order order = new Order(rs.getString("title"));
        order.setId(rs.getInt("id"));
        return order;
    }

    private OrderLine toOrderLine(Row row) throws SQLException {
        OrderLine orderLine = new OrderLine(row.getString("title"));
        orderLine.setProduct(ProductRepository.toProduct(row));
        orderLine.setAmount(row.getInt("amount"));
        return orderLine;
    }
}
