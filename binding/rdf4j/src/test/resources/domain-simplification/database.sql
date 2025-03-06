create table t1 (
    id text not null,
    ts timestamp not null,
    name text not null);

ALTER TABLE t1 ADD CONSTRAINT pk1 primary key (id, ts);

create table t2 (
    id text not null,
    ts timestamp not null,
    v text);

--ALTER TABLE t2 ADD CONSTRAINT pk2 primary key (id, ts, v);

ALTER TABLE t2 ADD CONSTRAINT fk1 FOREIGN KEY (id, ts)
REFERENCES t1 (id, ts) ;