CREATE TABLE "address" (
    "municipality" VARCHAR NOT NULL,
    "street" VARCHAR NOT NULL,
    "housenumber" INTEGER NOT NULL,
    "building_id" VARCHAR,
    PRIMARY KEY ("municipality", "street", "housenumber")
);

INSERT INTO "address" ("municipality", "street", "housenumber", "building_id") VALUES ('Bolzano', 'Via Verdi', 1, 'B1');
INSERT INTO "address" ("municipality", "street", "housenumber", "building_id") VALUES ('Bolzano', 'Via Roma', 2, null);
