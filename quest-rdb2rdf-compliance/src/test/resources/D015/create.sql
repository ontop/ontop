CREATE TABLE "Country" (
  "Code" VARCHAR(2),
  "Name" VARCHAR(100),
  "Lan" VARCHAR(10),
  PRIMARY KEY ("Code","Lan")
);
INSERT INTO "Country" ("Code", "Name", "Lan") VALUES ('BO', 'Bolivia, Plurinational State of', 'EN');
INSERT INTO "Country" ("Code", "Name", "Lan") VALUES ('BO', 'Estado Plurinacional de Bolivia', 'ES');
INSERT INTO "Country" ("Code", "Name", "Lan") VALUES ('IE', 'Ireland', 'EN');
INSERT INTO "Country" ("Code", "Name", "Lan") VALUES ('IE', 'Irlanda', 'ES');

