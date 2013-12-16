
CREATE TABLE "table1" (
    "uri" character varying(100) NOT NULL,
    "val" character varying(100),
);


INSERT INTO "table1" VALUES ('uri1', 'value1');

ALTER TABLE "table1"
    ADD CONSTRAINT "table1_pkey" PRIMARY KEY ("uri");


