
CREATE TABLE "table1" (
    "uri" character varying(100) NOT NULL,
    "value" character varying(100),
    "code" integer,
    "role" character varying(100), 
);


INSERT INTO "table1" VALUES ('uri1', 'A', '1', 'P');
INSERT INTO "table1" VALUES ('uri2', 'B', '2', 'P');
INSERT INTO "table1" VALUES ('uri3', 'A', '2', 'Q');
INSERT INTO "table1" VALUES ('uri4', 'B', '2', 'Q');


ALTER TABLE "table1"
    ADD CONSTRAINT "table1_pkey" PRIMARY KEY ("uri");


