CREATE DATABASE IF NOT EXISTS db_functions;

USE db_functions;

CREATE STABLE IF NOT EXISTS electricity ( ts timestamp, curr float, voltage int, phase double, description varchar(64)) TAGS ( location varchar(64),group_id int);

CREATE TABLE IF NOT EXISTS d1001 USING electricity ( location, group_id ) TAGS ( "San Francisco", 1 );

CREATE TABLE IF NOT EXISTS d1002 USING electricity ( location, group_id ) TAGS ( "Santa Monica", 2 );

INSERT INTO d1001 VALUES ("2018-10-03 14:38:05", 10.2, 220, 0.23, 'sensor1');
INSERT INTO d1001 VALUES ("2018-10-04 14:38:15", 12.6, 218, 0.33, 'test sensor 1');
INSERT INTO d1001 VALUES ("2018-10-05 14:38:25", 12.3, 221, NULL, NULL);

INSERT INTO d1002 VALUES ("2018-09-03 14:38:04", 10.2, 220, 0.23, 'second sensor first measure');
INSERT INTO d1002 VALUES ("2018-10-03 14:38:14", 10.3, 218, 0.25, NULL);

CREATE STABLE IF NOT EXISTS water ( ts timestamp, flow double, pressure int, above bool ) TAGS ( location varchar(64),group_id int);

CREATE TABLE IF NOT EXISTS d2001 USING water ( location, group_id ) TAGS ( "San Francisco", 1 );

INSERT INTO d2001 VALUES ("2018-10-03 14:38:05+01:00", 10.2, 220, true);
INSERT INTO d2001 VALUES ("2018-10-04 14:38:15+03:00", 12.6, 218, false);
INSERT INTO d2001 VALUES ("2018-10-05 14:38:25-05:00", 13.4, NULL, false);