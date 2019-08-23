create table piazza (
	id int primary key,
	geo varchar(255) NOT NULL
);

insert into piazza (id, geo) values (1, 'POLYGON((-77.089005 38.913574, -77.029953 38.913574, -77.029953 38.886321, -77.089005 38.886321, -77.089005 38.913574))');