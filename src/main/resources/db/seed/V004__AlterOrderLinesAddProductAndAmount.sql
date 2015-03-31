delete from order_lines;

alter table order_lines
  add product_id integer NOT NULL references products(id),
  add amount integer not null
;

