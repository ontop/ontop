CREATE TABLE T1 (
    id1 integer not null,
    id2 integer not null,
    price integer not null);
ALTER TABLE T1 ADD CONSTRAINT t1_pk PRIMARY KEY(id1, id2);

insert into T1 (id1, id2, price) values (1, 2, 5);
insert into T1 (id1, id2, price) values (1, 3, 7);
insert into T1 (id1, id2, price) values (2, 2, 100);

CREATE TABLE T2 (
    id2 integer unique not null);

insert into T2 (id2) values (2);
insert into T2 (id2) values (4);
