DROP TABLE IF EXISTS academic;

CREATE TABLE academic(
    "a_id" INT NOT NULL,
    "first_name" VARCHAR(40) NOT NULL,
    "last_name" VARCHAR(40) NOT NULL,
    "position" INT NOT NULL
);

INSERT INTO academic("a_id", "first_name", "last_name", "position") VALUES (1, 'Anna', 'Chambers', 1),
(1, 'Priscilla', 'Hildr', 2),
(3, 'Kyler', 'Josephina', 1),
(4, 'Gerard', 'Cosimo', 2);

DROP TABLE IF EXISTS teaching1;
CREATE TABLE teaching1 (
    "c_id" INT NOT NULL,
    "a_id" INT NOT NULL
);
INSERT INTO teaching1("c_id", "a_id") VALUES
(1234, 1),
(1235, 2);

DROP TABLE IF EXISTS teaching2;
CREATE TABLE teaching2 (
    "c_id" INT NOT NULL,
    "a_id" INT NOT NULL
);
INSERT INTO teaching2 ("c_id", "a_id") VALUES
(1236, 3),
(1237, 4);
