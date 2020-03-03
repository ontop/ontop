DROP TABLE IF EXISTS academic;
CREATE TABLE academic(
    "a_id" INT NOT NULL,
    "first_name" VARCHAR(40) NOT NULL,
    "last_name" VARCHAR(40) NOT NULL,
    "position" INT NOT NULL
);
INSERT INTO academic("a_id", "first_name", "last_name", "position") VALUES
(1, 'Anna', 'Chambers', 1),
(2, 'Edward', 'May', 9),
(3, 'Rachel', 'Ward', 8),
(4, 'Priscilla', 'Hildr', 2),
(5, 'Zlata', 'Richmal', 3),
(6, 'Nathaniel', 'Abolfazl', 4),
(7, 'Sergei', 'Elian', 5),
(8, 'Alois', 'Jayant', 6),
(9, 'Torborg', 'Chernobog', 7),
(10, 'Udi', 'Heinrike', 8),
(11, 'Alvena', 'Merry', 9),
(12, 'Kyler', 'Josephina', 1),
(13, 'Gerard', 'Cosimo', 2),
(14, 'Karine', 'Attilio', 3);

DROP TABLE IF EXISTS teaching1;
CREATE TABLE teaching1 (
    "c_id" INT NOT NULL,
    "a_id" INT NOT NULL
);
INSERT INTO teaching1 ("c_id", "a_id") VALUES
(1234, 1),
(1234, 2),
(1235, 1),
(1235, 3),
(1236, 4),
(1236, 8),
(1236, 9),
(1500, 12),
(1500, 2),
(1501, 12),
(1501, 14),
(1501, 7),
(1502, 13);

DROP TABLE IF EXISTS teaching2;
CREATE TABLE teaching2 (
    "c_id" INT NOT NULL,
    "a_id" INT NOT NULL
);
INSERT INTO teaching2 ("c_id", "a_id") VALUES
(1234, 1),
(1234, 2),
(1235, 1),
(1235, 3),
(1236, 4),
(1236, 8),
(1236, 9),
(1500, 12),
(1500, 2),
(1501, 12),
(1501, 14),
(1501, 7),
(1502, 13);
