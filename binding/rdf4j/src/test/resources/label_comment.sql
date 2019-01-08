CREATE TABLE "data" (
    id integer NOT NULL,
    label character varying(10),
    comment character varying(100),
    lang character varying(2)
);
INSERT INTO "data" VALUES (1, 'testdata', 'English description', 'en');
INSERT INTO "data" VALUES (1, 'testdaten', 'Deutsche Beschreibung', 'de');