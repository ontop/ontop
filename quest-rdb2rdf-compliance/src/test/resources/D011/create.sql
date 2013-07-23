CREATE TABLE "Student" (
"ID" integer PRIMARY KEY,
"FirstName" varchar(50),
"LastName" varchar(50)
);
CREATE TABLE "Sport" (
"ID" integer PRIMARY KEY,
"Description" varchar(50)
);
CREATE TABLE "Student_Sport" (
"ID_Student" integer,
"ID_Sport" integer,
PRIMARY KEY ("ID_Student","ID_Sport"),
FOREIGN KEY ("ID_Student") REFERENCES "Student"("ID"),
FOREIGN KEY ("ID_Sport") REFERENCES "Sport"("ID")
);

INSERT INTO "Student" ("ID","FirstName","LastName") VALUES (10,'Venus', 'Williams');
INSERT INTO "Student" ("ID","FirstName","LastName") VALUES (11,'Fernando', 'Alonso');
INSERT INTO "Student" ("ID","FirstName","LastName") VALUES (12,'David', 'Villa');

INSERT INTO "Sport" ("ID", "Description") VALUES (110,'Tennis');
INSERT INTO "Sport" ("ID", "Description") VALUES (111,'Football');
INSERT INTO "Sport" ("ID", "Description") VALUES (112,'Formula1');

INSERT INTO "Student_Sport" ("ID_Student", "ID_Sport") VALUES (10,110);
INSERT INTO "Student_Sport" ("ID_Student", "ID_Sport") VALUES (11,111);
INSERT INTO "Student_Sport" ("ID_Student", "ID_Sport") VALUES (11,112);
INSERT INTO "Student_Sport" ("ID_Student", "ID_Sport") VALUES (12,111);
