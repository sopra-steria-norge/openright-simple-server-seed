delete from order_lines;

alter table order_lines add COLUMN product_id integer NOT NULL;
alter table order_lines add FOREIGN KEY (product_id) references products(id);
alter table order_lines add COLUMN amount integer not null;

