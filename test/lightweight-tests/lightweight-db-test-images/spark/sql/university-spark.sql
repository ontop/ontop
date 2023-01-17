CREATE DATABASE university;

CREATE TABLE university.professors (
                            prof_id int,
                            first_name varchar(100) NOT NULL,
                            last_name varchar(100) NOT NULL,
                            nickname varchar(100)
);

INSERT INTO university.professors VALUES (1, 'Roger', 'Smith', 'Rog');
INSERT INTO university.professors VALUES (2, 'Frank', 'Pitt', 'Frankie');
INSERT INTO university.professors VALUES (3, 'John', 'Depp', 'Johnny');
INSERT INTO university.professors VALUES (4, 'Michael', 'Jackson', 'King of Pop');
INSERT INTO university.professors VALUES (5, 'Diego', 'Gamper', NULL);
INSERT INTO university.professors VALUES (6, 'Johann', 'Helmer', NULL);
INSERT INTO university.professors VALUES (7, 'Barbara', 'Dodero', NULL);
INSERT INTO university.professors VALUES (8, 'Mary', 'Poppins', NULL);

CREATE TABLE university.course (
                        course_id varchar(100),
                        nb_students int NOT NULL,
                        duration decimal NOT NULL
);

INSERT INTO university.course VALUES ('LinearAlgebra', 10, 24.5);
INSERT INTO university.course VALUES ('DiscreteMathematics', 11, 30);
INSERT INTO university.course VALUES ('AdvancedDatabases', 12, 20);
INSERT INTO university.course VALUES ('ScientificWriting', 13, 18);
INSERT INTO university.course VALUES ('OperatingSystems', 10, 30);

CREATE TABLE university.teaching (
                          course_id varchar(100) NOT NULL,
                          prof_id int NOT NULL
);

INSERT INTO university.teaching VALUES ('LinearAlgebra', 1);
INSERT INTO university.teaching VALUES ('DiscreteMathematics', 1);
INSERT INTO university.teaching VALUES ('AdvancedDatabases', 3);
INSERT INTO university.teaching VALUES ('ScientificWriting', 8);
INSERT INTO university.teaching VALUES ('OperatingSystems', 1);
