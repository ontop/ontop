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

CREATE TABLE IF NOT EXISTS personxt (
                                    id integer NOT NULL,
                                    ssn integer,
                                    fullname varchar(100),
                                    tags varchar(6000),
                                    friends varchar(6000)
);

INSERT INTO personxt VALUES (1,
                            123,
                            'Mary Poppins',
                            '[111, 222, 333]',
                            '[{ "fname": "Alice", "nickname": "Al", "address": { "city": "Bolzano", "street": "via Roma", "number": "33" }}, { "fname": "Robert", "nickname": "Bob", "address": {"city": "Merano", "street": "via Dante", "number": "23" }}]'
                            );

INSERT INTO personxt VALUES (2,
                            1234,
                            'Roger Rabbit',
                            '[111, 222]',
                            '{ "fname": "Mickey", "lname": "Mouse"}'
                            );

INSERT INTO personxt VALUES (3,
                            23,
                            'Bob Loblaw',
                            NULL,
                            '[]'
                            );

INSERT INTO personxt VALUES (4,
                            24,
                            'Kenny McCormick',
                            '[]',
                            NULL
                            );



CREATE TABLE IF NOT EXISTS person (
                                    id integer NOT NULL,
                                    name varchar(100),
                                    publication varchar(6000),
                                    contribs varchar(6000)
);

INSERT INTO person VALUES (
                          	1,
                          	'Sanjay Ghemawat',
                          	'[ { "title": "The Google file system", "id": 1, "year": 2003, "venue":"SOSP", "editor": [ {"name": "M. Scott"}, {"name": "L. Peterson"} ] }, { "title": "Bigtable: A Distributed Storage System for Structured Data", "id": 2, "year": 2008, "venue":"ACM TOCS" , "editor": [ {"name": "M. Swift"} ] }, { "title": "MapReduce: Simplified Data Processing on Large Clusters", "id": 3, "year": 2004, "venue":"OSDI", "editor": [ {"name": "E. Brewer"}, {"name": "P. Chen"} ] } ]',
                          	'[ {"value": "Google File System"}, {"value": "MapReduce "}, {"value": "Bigtable "}, {"value": "Spanner "} ]'
                          );

INSERT INTO person VALUES (2,
                            'Jeffrey Dean',
                            '[ { "title": "Bigtable: A Distributed Storage System for Structured Data", "id": 2, "year": 2008, "venue":"ACM TOCS", "editor": [ {"name": "M. Swift"} ] }, { "title": "MapReduce: Simplified Data Processing on Large Clusters", "id": 3, "year": 2004, "venue":"OSDI", "editor": [ {"name": "E. Brewer"}, {"name": "P. Chen"} ] }, { "title": "Large Scale Distributed Deep Networks", "id": 4, "year": 2012, "venue":"NeurIPS", "editor": [ {"name": "P. Bartlett"}, {"name": "F. Pereira"}, {"name": "C. Burges"}, {"name": "L. Bottou"}, {"name": "K. Weinberger "} ] } ]',
                            '[ {"value": "MapReduce "}, {"value": "Bigtable "}, {"value": "Spanner "}, {"value": "TensorFlow "} ]'
                            );

