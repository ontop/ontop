CREATE TABLE person (
    person_id integer NOT NULL ,
    first_name text,
    PRIMARY KEY (person_id)
);
INSERT INTO person VALUES (1, 'Mary');
INSERT INTO person VALUES (2, 'John');
INSERT INTO person VALUES (3, NULL);
INSERT INTO person VALUES (4, 'James');

CREATE TABLE house (
    house_id integer NOT NULL ,
    address text NOT NULL,
    owner integer,
    PRIMARY KEY (house_id)
);
INSERT INTO house VALUES (100, 'Via Roma, 14', 1);
INSERT INTO house VALUES (200, 'Piazza Verdi, 1', 1);
INSERT INTO house VALUES (300, 'Via Manci, 31', 2);
INSERT INTO house VALUES (400, 'Via Venezia, 27', NULL);

CREATE TABLE sensor (
    sensor_id integer NOT NULL ,
   timestamp text NOT NULL,
    PRIMARY KEY (sensor_id)
);

INSERT INTO sensor VALUES (1, '1949-12-31 14:55:00.000000');
INSERT INTO sensor VALUES (2, '1949-12-31 14:55:00.000000');