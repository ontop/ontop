-- Spark SQL CLI seems to have issues with multiple files
--CREATE DATABASE books;

CREATE TABLE books (
                       id integer NOT NULL,
                       title varchar(100),
                       price integer,
                       discount numeric,
                       description varchar(100),
                       lang varchar(100),
                       publication_date timestamp
);

INSERT INTO books VALUES (1, 'SPARQL Tutorial', 43, 0.2, 'good', 'en', CAST('2014-06-05 16:47:52' AS TIMESTAMP));
INSERT INTO books VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', CAST('2011-12-08 11:30:00' AS TIMESTAMP));
INSERT INTO books VALUES (3, 'Crime and Punishment', 34, 0.2, 'good', 'en', CAST('2015-09-21 09:23:06' AS TIMESTAMP));
INSERT INTO books VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', CAST('1970-11-05 07:50:00' AS TIMESTAMP));



--CREATE DATABASE university;

CREATE TABLE professors (
                                       prof_id int,
                                       first_name varchar(100) NOT NULL,
                                       last_name varchar(100) NOT NULL,
                                       nickname varchar(100)
);

INSERT INTO professors VALUES (1, 'Roger', 'Smith', 'Rog');
INSERT INTO professors VALUES (2, 'Frank', 'Pitt', 'Frankie');
INSERT INTO professors VALUES (3, 'John', 'Depp', 'Johnny');
INSERT INTO professors VALUES (4, 'Michael', 'Jackson', 'King of Pop');
INSERT INTO professors VALUES (5, 'Diego', 'Gamper', NULL);
INSERT INTO professors VALUES (6, 'Johann', 'Helmer', NULL);
INSERT INTO professors VALUES (7, 'Barbara', 'Dodero', NULL);
INSERT INTO professors VALUES (8, 'Mary', 'Poppins', NULL);

CREATE TABLE course (
                                   course_id varchar(100),
                                   nb_students int NOT NULL,
                                   duration decimal NOT NULL
);

INSERT INTO course VALUES ('LinearAlgebra', 10, 24.5);
INSERT INTO course VALUES ('DiscreteMathematics', 11, 30);
INSERT INTO course VALUES ('AdvancedDatabases', 12, 20);
INSERT INTO course VALUES ('ScientificWriting', 13, 18);
INSERT INTO course VALUES ('OperatingSystems', 10, 30);

CREATE TABLE teaching (
                                     course_id varchar(100) NOT NULL,
                                     prof_id int NOT NULL
);

INSERT INTO teaching VALUES ('LinearAlgebra', 1);
INSERT INTO teaching VALUES ('DiscreteMathematics', 1);
INSERT INTO teaching VALUES ('AdvancedDatabases', 3);
INSERT INTO teaching VALUES ('ScientificWriting', 8);
INSERT INTO teaching VALUES ('OperatingSystems', 1);
