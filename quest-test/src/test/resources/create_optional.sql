
CREATE TABLE "address" (
  "id" int DEFAULT NULL,
  "city" varchar(50) DEFAULT NULL
);
INSERT INTO "address" VALUES (1,'Bolzano'),(2,'Rome');

CREATE TABLE "people" (
  "id" int NOT NULL,
  "firstname" varchar(100) DEFAULT NULL,
  "age" int DEFAULT NULL,
  "mbox" varchar(100) DEFAULT NULL,
  "nick1" varchar(100) DEFAULT NULL,
  "nick2" varchar(100) DEFAULT NULL,
  PRIMARY KEY ("id")
) ;

INSERT INTO "people" VALUES (1,'Alice',18,'alice@example.org','alice1','alice2'),(2,'Bob',19,'bob@example.org','bob1',NULL),(3,'Eve',20,'eve@example.org',NULL,NULL),(4,'Mark',NULL,'mark@example.org','mark1',NULL);

CREATE TABLE "salary1" (
  "id" int DEFAULT NULL,
  "salary" int DEFAULT NULL
) ;

INSERT INTO "salary1" VALUES (1,1000),(2,2000);

CREATE TABLE "salary2" (
  "id" int DEFAULT NULL,
  "salary" int DEFAULT NULL
) ;

INSERT INTO "salary2" VALUES (3,1500),(4,2500);
