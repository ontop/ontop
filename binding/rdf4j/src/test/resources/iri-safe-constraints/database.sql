CREATE TABLE "address" (
    "municipality" VARCHAR NOT NULL,
    "street" VARCHAR NOT NULL,
    "housenumber" VARCHAR NOT NULL,
    "building_id" VARCHAR,
    PRIMARY KEY ("municipality", "street", "housenumber")
);

INSERT INTO "address" ("municipality", "street", "housenumber", "building_id") VALUES ('Bolzano', 'Via Verdi', '1', 'B1');
