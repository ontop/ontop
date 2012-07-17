CREATE TABLE "table1" (
    "id" integer NOT NULL,
    "name" character varying(100) NOT NULL,
    "value" integer NOT NULL,
);

CREATE TABLE "table2" (
    "sid" integer NOT NULL,
    "id" integer NOT NULL,
);

CREATE TABLE "table3" (
    "id" integer NOT NULL,
    "variable" character varying(100) NOT NULL,
    "value" integer NOT NULL,
);

INSERT INTO "table1" VALUES (100, 'a', 1);
INSERT INTO "table1" VALUES (101, 'b', 2);
INSERT INTO "table1" VALUES (102, 'c', 3);
INSERT INTO "table1" VALUES (103, 'd', 4);

INSERT INTO "table2" VALUES (200, 100);
INSERT INTO "table2" VALUES (201, 100);
INSERT INTO "table2" VALUES (202, 101);
INSERT INTO "table2" VALUES (203, 102);
INSERT INTO "table2" VALUES (204, 102);
INSERT INTO "table2" VALUES (205, 102);
INSERT INTO "table2" VALUES (206, 103);
INSERT INTO "table2" VALUES (207, 104);

INSERT INTO "table3" VALUES (100, 'A', 567);
INSERT INTO "table3" VALUES (101, 'B', 328);
INSERT INTO "table3" VALUES (102, 'C', 954);
INSERT INTO "table3" VALUES (103, 'D', 214);

ALTER TABLE "table1"
    ADD CONSTRAINT "table1_pkey" PRIMARY KEY ("id");

ALTER TABLE "table2"
    ADD CONSTRAINT "table2_pkey" PRIMARY KEY ("sid");
    
ALTER TABLE "table3"
    ADD CONSTRAINT "table3_pkey" PRIMARY KEY ("id");

