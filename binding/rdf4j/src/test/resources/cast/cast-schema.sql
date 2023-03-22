CREATE TABLE ratings (
                         ID serial PRIMARY KEY,
                         rating_string VARCHAR (1) NOT NULL );
INSERT INTO ratings (id, rating_string) VALUES (1, 'A'), (2, 'B'), (3, 'C'), (4, 1), (5, 2), (6, 3);

CREATE TABLE ratings_float (
                               ID serial PRIMARY KEY,
                               rating_float FLOAT NOT NULL );

INSERT INTO ratings_float (ID, rating_float) VALUES (1, 0.1), (2, 0.2), (3, 0.3), (4, 0.04), (5, 5.05), (6, 6.66);

CREATE TABLE ratings_double (
                                ID serial PRIMARY KEY,
                                rating_double DOUBLE PRECISION NOT NULL );
INSERT INTO ratings_double (ID, rating_double) VALUES (1, 0.1), (2, 0.2), (3, 0.3), (4, 0.04), (5, 5.05), (6, 6.66);

CREATE TABLE ratings_decimal (
                                 ID serial PRIMARY KEY,
                                 rating_decimal DOUBLE PRECISION NOT NULL );
INSERT INTO ratings_decimal (ID, rating_decimal) VALUES (1, 0.1), (2, 0.2), (3, 0.3), (4, 0.04), (5, 5.05), (6, 6.66);

CREATE TABLE ratings_integer (
                                 ID serial PRIMARY KEY,
                                 rating_integer BIGINT NOT NULL );
INSERT INTO ratings_integer (ID, rating_integer) VALUES (1, 0), (2, 0), (3, 0), (4, 0), (5, 5), (6, 7);

CREATE TABLE ratings_boolean (
                                 ID serial PRIMARY KEY,
                                 rating_boolean BOOLEAN NOT NULL );
INSERT INTO ratings_boolean (ID, rating_boolean) VALUES (1, FALSE), (2, FALSE), (3, FALSE), (4, FALSE), (5, TRUE), (6, TRUE);

CREATE TABLE dates (
                       ID serial PRIMARY KEY,
                       finaldate DATE NOT NULL );
INSERT INTO dates (ID, finaldate) VALUES (1, '1999-12-10'), (2, '1998-12-10'), (3, '1997-12-13');

CREATE TABLE dates_string (
                              ID serial PRIMARY KEY,
                              finaldate VARCHAR (10) NOT NULL );
INSERT INTO dates_string (ID, finaldate) VALUES (1, '1999-12-10'), (2, '1998-12-10'), (3, '1997-12-13'), (4, 1), (5, 2), (6, 3);

CREATE TABLE datetimes (
                           ID serial PRIMARY KEY,
                           finaldatetime TIMESTAMP NOT NULL );
INSERT INTO datetimes (ID, finaldatetime) VALUES (1, '1999-12-10 11:00:00+03'), (2, '1998-12-10 11:15:00+03'),
                                                 (3, '1997-12-13 11:00:06');

CREATE TABLE datetimes_string (
                                  ID serial PRIMARY KEY,
                                  finaldatetime VARCHAR (25) NOT NULL );
INSERT INTO datetimes_string (ID, finaldatetime) VALUES (1, '1999-12-10 11:00:00+03'), (2, '1998-12-10 11:15:00+03'),
                                                        (3, '1997-12-13 11:00:06'), (4, 1), (5, 2), (6, 3);