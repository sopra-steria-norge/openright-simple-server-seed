create table order_lines (
	id int primary key,
	order_id integer NOT NULL references orders(id),
	title text not null
);