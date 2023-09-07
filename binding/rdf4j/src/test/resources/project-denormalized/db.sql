CREATE TABLE "project"(
    "id" INT NOT NULL,
    "mun_id" INT,
    "mun_label" VARCHAR(40),
    "province_id" INT,
    "province_label" VARCHAR(40),
    "region_id" INT,
    "region_label" VARCHAR(40)
);
ALTER TABLE "project" ADD CONSTRAINT CONSTRAINT_8 PRIMARY KEY("id");