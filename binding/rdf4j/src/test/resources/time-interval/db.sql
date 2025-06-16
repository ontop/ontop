create table "person" (
    id varchar(100) not null,
    name varchar(100),
    age int,
    birth_date timestamp,
    primary key (id)
);

insert into "person" (id, name, age, birth_date) values
('1', 'Alice', 30, '1993-01-01 13:15:00'),
('2', 'Bob', 25, '1998-02-02 14:20:00');
