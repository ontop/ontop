CREATE TABLE "weight_measurement"
(
    "id"     int primary key,
    "weight" decimal -- (in kg)
);

CREATE TABLE "height_measurement"
(
    "id"     int primary key,
    "height" decimal,
    "unit"   varchar(2) -- (cm or in)
);
