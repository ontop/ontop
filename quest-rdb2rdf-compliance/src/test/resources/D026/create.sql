CREATE TABLE "SportType" (
"ID1" integer,
"Name" varchar (50),
PRIMARY KEY ("ID1")
);

CREATE TABLE "Sport" (
"ID2" integer,
"Name" varchar (50),
"SType" integer,
PRIMARY KEY ("ID2")
);

CREATE TABLE "Student" (
"ID3" integer,
"Name" varchar(50),
"Sport" integer,
PRIMARY KEY ("ID3")
);

INSERT INTO "SportType" ("ID1", "Name") VALUES (1,'TennisType');
INSERT INTO "Sport" ("ID2", "Name", "SType") VALUES (100,'Tennis', 1);
INSERT INTO "Student" ("ID3", "Name", "Sport") VALUES (10,'Venus Williams', 100);
INSERT INTO "Student" ("ID3", "Name", "Sport") VALUES (20,'Demi Moore', NULL);

