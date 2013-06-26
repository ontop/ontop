CREATE TABLE "Addresses" (
	"ID" INT,
	PRIMARY KEY("ID"),
	"city" VARCHAR(10),
	"state" CHAR(2)
);

CREATE TABLE "Department" (
	"ID" INT,
	PRIMARY KEY("ID"),
	"name" VARCHAR(50),
	"city" VARCHAR(50),
	UNIQUE ("name", "city"),
	"manager" INT
);

CREATE TABLE "People" (
	"ID" INT,
	PRIMARY KEY("ID"),
	"fname" VARCHAR(10),
	"addr" INT,
	FOREIGN KEY ("addr") REFERENCES "Addresses"("ID"),
	"deptName" VARCHAR(50),
	"deptCity" VARCHAR(50)
);

ALTER TABLE "Department" ADD FOREIGN KEY("manager") REFERENCES "People"("ID");
ALTER TABLE "People" ADD FOREIGN KEY("deptName", "deptCity") REFERENCES "Department"("name", "city");

INSERT INTO "Addresses" ("ID", "city",      "state")
                 VALUES (18,   'Cambridge', 'MA');

INSERT INTO "People" ("ID", "fname", "addr", "deptName", "deptCity" )
              VALUES (8,    'Sue',   NULL,   NULL,       NULL);

INSERT INTO "Department" ("ID", "name",       "city",      "manager")
                  VALUES (23,   'accounting', 'Cambridge', 8);

INSERT INTO "People" ("ID", "fname", "addr", "deptName",   "deptCity" )
              VALUES (7,    'Bob',   18,     'accounting', 'Cambridge');
              
CREATE TABLE "Projects" (
	"lead" INT,
	"name" VARCHAR(50), 
	UNIQUE ("lead", "name"), 
	"deptName" VARCHAR(50), 
	"deptCity" VARCHAR(50),
	UNIQUE ("name", "deptName", "deptCity"),
	FOREIGN KEY ("deptName", "deptCity") REFERENCES "Department"("name", "city")
);
CREATE TABLE "TaskAssignments" (
	"worker" INT,
	"project" VARCHAR(50), 
	PRIMARY KEY ("worker", "project"), 
	"deptName" VARCHAR(50), 
	"deptCity" VARCHAR(50),
	FOREIGN KEY ("project", "deptName", "deptCity") REFERENCES "Projects"("name", "deptName", "deptCity"),
	FOREIGN KEY ("deptName", "deptCity") REFERENCES "Department"("name", "city")
);

ALTER TABLE "Projects" ADD FOREIGN KEY("lead") REFERENCES "People"("ID");
ALTER TABLE "TaskAssignments" ADD FOREIGN KEY("worker") REFERENCES "People"("ID");

INSERT INTO  "Projects" ("lead", "name",          "deptName",   "deptCity" )
                 VALUES (8,      'pencil survey', 'accounting', 'Cambridge');
INSERT INTO  "Projects" ("lead", "name",          "deptName",   "deptCity" )
                 VALUES (8,      'eraser survey', 'accounting', 'Cambridge');
INSERT INTO "TaskAssignments" ("worker", "project",       "deptName",   "deptCity" )
                       VALUES (7,        'pencil survey', 'accounting', 'Cambridge');              