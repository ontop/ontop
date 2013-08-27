
CREATE TABLE "table1" (
    "uri" character varying(100) NOT NULL,
    "value" character varying(100),
);


INSERT INTO "table1" VALUES ('uri1', 'A');
INSERT INTO "table1" VALUES ('uri2', 'B');
INSERT INTO "table1" VALUES ('uri3', 'A');
INSERT INTO "table1" VALUES ('uri4', 'B');


ALTER TABLE "table1"
    ADD CONSTRAINT "table1_pkey" PRIMARY KEY ("uri");


