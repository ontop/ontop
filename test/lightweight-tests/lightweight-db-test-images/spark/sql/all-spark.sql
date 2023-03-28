-- Spark SQL CLI seems to have issues with multiple files

CREATE TABLE IF NOT EXISTS books (
                       id integer NOT NULL,
                       title varchar(100),
                       price integer,
                       discount decimal(10,2),
                       description varchar(100),
                       lang varchar(100),
                       publication_date timestamp
);

INSERT INTO books VALUES (1, 'SPARQL Tutorial', 43, 0.2, 'good', 'en', CAST('2014-06-05 16:47:52' AS TIMESTAMP));
INSERT INTO books VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', CAST('2011-12-08 11:30:00' AS TIMESTAMP));
INSERT INTO books VALUES (3, 'Crime and Punishment', 34, 0.2, 'good', 'en', CAST('2015-09-21 09:23:06' AS TIMESTAMP));
INSERT INTO books VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', CAST('1970-11-05 07:50:00' AS TIMESTAMP));



--CREATE DATABASE university;

CREATE TABLE IF NOT EXISTS professors (
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

CREATE TABLE IF NOT EXISTS course (
                                   course_id varchar(100),
                                   nb_students int NOT NULL,
                                   duration decimal(10,1) NOT NULL
);

INSERT INTO course VALUES ('LinearAlgebra', 10, 24.5);
INSERT INTO course VALUES ('DiscreteMathematics', 11, 30);
INSERT INTO course VALUES ('AdvancedDatabases', 12, 20);
INSERT INTO course VALUES ('ScientificWriting', 13, 18);
INSERT INTO course VALUES ('OperatingSystems', 10, 30);

CREATE TABLE IF NOT EXISTS teaching (
                                     course_id varchar(100) NOT NULL,
                                     prof_id int NOT NULL
);

INSERT INTO teaching VALUES ('LinearAlgebra', 1);
INSERT INTO teaching VALUES ('DiscreteMathematics', 1);
INSERT INTO teaching VALUES ('AdvancedDatabases', 3);
INSERT INTO teaching VALUES ('ScientificWriting', 8);
INSERT INTO teaching VALUES ('OperatingSystems', 1);

CREATE TABLE IF NOT EXISTS company_data (
    id integer NOT NULL,
    days varchar(10000),
    income varchar(10000),
    workers varchar(10000),
    managers varchar(10000)
);

INSERT INTO company_data VALUES (1,  '["2023-01-01 18:00:00", "2023-01-15 18:00:00", "2023-01-29 12:00:00"]', '[10000, 18000, 13000]', '[["Sam", "Cynthia"], ["Bob"], ["Jim"]]', '[{"firstName": "Mary", "lastName": "Jane", "age": 28}, {"firstName": "Carlos", "lastName": "Carlson", "age": 45}, {"firstName": "John", "lastName": "Moriarty", "age": 60}]');
INSERT INTO company_data VALUES (2,  '["2023-02-12 18:00:00", "2023-02-26 18:00:00"]', '[14000, 0]', '[["Jim", "Cynthia"], []]', '[{"firstName": "Helena", "lastName": "of Troy"}, {"firstName": "Robert", "lastName": "Smith", "age": 48}]');
INSERT INTO company_data VALUES (3,  '["2023-03-12 18:00:00", "2023-03-26 18:00:00"]', '[15000, 20000]', '[["Carl", "Bob", "Cynthia"], ["Jim", "Bob"]]', '[{"firstName": "Joseph", "lastName": "Grey"}, {"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}]');
INSERT INTO company_data VALUES (4,  '[]', '[]', NULL, '[]');

CREATE TABLE company_data_arrays (
    id integer NOT NULL,
    days array<timestamp>,
    income array<integer>,
    workers array<array<string>>,
    managers array<string>
);

INSERT INTO company_data_arrays VALUES (1,  array(CAST('2023-01-01 18:00:00' AS TIMESTAMP), CAST('2023-01-15 18:00:00' AS TIMESTAMP), CAST('2023-01-29 12:00:00' AS TIMESTAMP)), array(10000, 18000, 13000), array(array('Sam', 'Cynthia'), array('Bob'), array('Jim')), array('{"firstName": "Mary", "lastName": "Jane", "age": 28}', '{"firstName": "Carlos", "lastName": "Carlson", "age": 45}', '{"firstName": "John", "lastName": "Moriarty", "age": 60}'));
INSERT INTO company_data_arrays VALUES (2,  array(CAST('2023-02-12 18:00:00' AS TIMESTAMP), CAST('2023-02-26 18:00:00' AS TIMESTAMP)), array(14000, 0), array(array('Jim', 'Cynthia'), array()), array('{"firstName": "Helena", "lastName": "of Troy"}', '{"firstName": "Robert", "lastName": "Smith", "age": 48}'));
INSERT INTO company_data_arrays VALUES (3,  array(CAST('2023-03-12 18:00:00' AS TIMESTAMP), CAST('2023-03-26 18:00:00' AS TIMESTAMP)), array(15000, 20000), array(array('Carl', 'Bob', 'Cynthia'), array('Jim', 'Bob', NULL)), array('{"firstName": "Joseph", "lastName": "Grey"}', '{"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}'));
INSERT INTO company_data_arrays VALUES (4,  array(), array(), NULL, array());

