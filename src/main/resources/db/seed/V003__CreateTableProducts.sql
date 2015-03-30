create table Products (
	id serial primary key,
	title text NOT NULL,
	description text NULL,
	active boolean not null default true
);

