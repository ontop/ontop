CREATE TABLE station (
    id integer PRIMARY KEY NOT NULL,
    stationtype varchar NOT NULL,
    origin varchar NOT NULL
);

CREATE TABLE measurement (
    id integer PRIMARY KEY NOT NULL,
    double_value double not null,
    type_id integer not null,
    station_id integer not null,
    period integer not null,
    FOREIGN KEY (station_id) REFERENCES station(id)
);

INSERT INTO station VALUES
    (1, 'Bicycle', 'ALGORAB'),
    (2, 'NOI-Place', 'test-origin'),
    (3, 'MeteoStation', 'test-origin'),
    (4, 'test-type', 'APPABZ'),
    (5, 'ParkingStation', 'FBK');

INSERT INTO measurement VALUES
    (1, 25.0, 4, 1, 3600),
    (2, 45.0, 4, 1, 7200),
    (3, 15.0, 3, 2, 7200),
    (4, 22.0, 4, 2, 7200),
    (5, 49.0, 8, 3, 7200),
    (6, 109.0, 4, 3, 7200),
    (7, 33.0, 5, 4, 7200),
    (8, 35.0, 6, 4, 3600),
    (9, 31.0, 8, 5, 7200),
    (10, 12.0, 4, 5, 7200);
