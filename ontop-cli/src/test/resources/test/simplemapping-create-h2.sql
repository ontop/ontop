
CREATE TABLE table1 (
    id int NOT NULL,
    attr1 int NOT NULL
);

ALTER TABLE table1
    ADD CONSTRAINT table1_pkey PRIMARY KEY (id);

INSERT INTO table1 VALUES ('1', '1');
INSERT INTO table1 VALUES ('3', '3');
INSERT INTO table1 VALUES ('5', '5');
INSERT INTO table1 VALUES ('7', '7');





