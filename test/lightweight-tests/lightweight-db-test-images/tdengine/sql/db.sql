CREATE DATABASE IF NOT EXISTS power;

USE power;

CREATE STABLE IF NOT EXISTS meters ( ts timestamp, curr float, voltage int, phase float ) TAGS ( location varchar(64),group_id int);

CREATE TABLE IF NOT EXISTS d1001 USING meters ( location, group_id ) TAGS ( "California.SanFrancisco", 1 );

CREATE TABLE IF NOT EXISTS d1002 USING meters ( location, group_id ) TAGS ( "California.SantaMonica", 2 );

INSERT INTO d1001 VALUES ("2018-10-03 14:38:05", 10.2, 220, 0.23), ("2018-10-03 14:38:15", 12.6, 218, 0.33), ("2018-10-03 14:38:25", 12.3, 221, 0.31);
INSERT INTO d1002 VALUES ("2018-10-03 14:38:04", 10.2, 220, 0.23), ("2018-10-03 14:38:14", 10.3, 218, 0.25), ("2018-10-03 14:38:24", 10.1, 220, 0.22);