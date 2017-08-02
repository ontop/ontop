CREATE TABLE "Sport" (
"ID" integer,
"Name" varchar (50),
PRIMARY KEY ("ID")
);

CREATE TABLE "Student" (
"ID" integer,
"Name" varchar(50),
"Sport" integer,
PRIMARY KEY ("ID"),
FOREIGN KEY("Sport") REFERENCES "Sport"("ID")
);

INSERT INTO "Sport" ("ID", "Name") VALUES (100,'Tennis');
INSERT INTO "Student" ("ID", "Name", "Sport") VALUES (10,'Venus Williams', 100);
INSERT INTO "Student" ("ID", "Name", "Sport") VALUES (20,'Demi Moore', NULL);

