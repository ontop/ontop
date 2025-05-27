CREATE DATABASE IF NOT EXISTS db_constraints;

USE db_constraints;

CREATE STABLE IF NOT EXISTS pressure_measurements ( ts timestamp, description varchar(64) PRIMARY KEY, observation float ) TAGS ( group_id int);

CREATE TABLE IF NOT EXISTS d1001 USING pressure_measurements ( group_id ) TAGS ( 1 );

CREATE TABLE IF NOT EXISTS d1002 USING pressure_measurements ( group_id ) TAGS ( 2 );

INSERT INTO d1001 VALUES ("2022-07-01 17:30:15", 'pressure_sensor1', 10.2);
INSERT INTO d1001 VALUES ("2023-12-30", 'pressure_sensor2', 32.6);
INSERT INTO d1002 VALUES ("2021-01-01 12:00:00", 'sensor3', 15.3);

CREATE STABLE IF NOT EXISTS temperature_measurements ( ts timestamp, description varchar(100), observation float ) TAGS ( group_id int);
ALTER TABLE temperature_measurements ADD CONSTRAINT fk_temperature FOREIGN KEY (ts, group_id) REFERENCES pressure_measurements(ts, group_id);

CREATE TABLE IF NOT EXISTS d2001 USING temperature_measurements ( group_id ) TAGS ( 1 );

INSERT INTO d2001 VALUES ("2022-07-01 17:30:15", 'temperature_sensor1', 20.5);
INSERT INTO d2001 VALUES ("2023-12-30", 'temperature_sensor2', 25.0);
