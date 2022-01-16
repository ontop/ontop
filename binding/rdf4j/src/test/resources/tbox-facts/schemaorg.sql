DROP SCHEMA IF EXISTS "model";

CREATE SCHEMA "model";

CREATE TABLE "model"."3dmodel"(
    "m_id" INT NOT NULL PRIMARY KEY
);

INSERT INTO "model"."3dmodel"
("m_id") VALUES
             (1),
             (2),
             (3),
             (4);