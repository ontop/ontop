DROP TABLE IF EXISTS sensors;
CREATE TABLE sensors (
    sensor_id INTEGER PRIMARY KEY,
    label VARCHAR NOT NULL,
    location VARCHAR NOT NULL
);

DROP TABLE IF EXISTS devices;
CREATE TABLE devices (
    device_id INTEGER PRIMARY KEY,
    model VARCHAR NOT NULL,
    firmware VARCHAR NOT NULL
);

DROP TABLE IF EXISTS observations;
CREATE TABLE observations (
    observation_id INTEGER PRIMARY KEY,
    sensor_id INTEGER NOT NULL REFERENCES sensors(sensor_id),
    device_id INTEGER NOT NULL REFERENCES devices(device_id),
    temperature DOUBLE NOT NULL,
    confidence DECIMAL(4,3) NOT NULL,
    observed_at TIMESTAMP NOT NULL
);

INSERT INTO sensors VALUES
    (101, 'Boiler room sensor', 'Basement'),
    (102, 'Lobby sensor', 'Entrance'),
    (103, 'Greenhouse sensor', 'Roof');

INSERT INTO devices VALUES
    (201, 'EdgeNode-A1', '1.4.2'),
    (202, 'EdgeNode-A1', '1.4.3'),
    (203, 'EdgeNode-Beta', '2.0.0');

INSERT INTO observations VALUES
    (9001, 101, 201, 68.4, 0.982, TIMESTAMP '2025-11-21 08:15:00'),
    (9002, 101, 202, 67.9, 0.965, TIMESTAMP '2025-11-21 09:45:00'),
    (9003, 102, 202, 70.1, 0.954, TIMESTAMP '2025-11-21 10:10:00'),
    (9004, 103, 203, 77.3, 0.993, TIMESTAMP '2025-11-21 11:25:00');
