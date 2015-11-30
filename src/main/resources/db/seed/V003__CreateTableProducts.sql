create table Products (
	id int primary key,
	title text NOT NULL,
	description text NULL,
	active boolean not null default true,
	price decimal
);

