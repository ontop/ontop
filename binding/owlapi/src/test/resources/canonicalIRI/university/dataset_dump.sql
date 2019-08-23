CREATE SCHEMA "uni1";

CREATE TABLE "uni1"."student" (
"s_id" INT NOT NULL PRIMARY KEY,
"s_ssn" VARCHAR(40) UNIQUE,
"first_name" VARCHAR(40) NOT NULL,
"last_name" VARCHAR(40) NOT NULL
);

CREATE TABLE "uni1"."academic" (
"a_id" INT NOT NULL PRIMARY KEY,
"a_ssn" VARCHAR(40) UNIQUE,
"first_name" VARCHAR(40) NOT NULL,
"last_name" VARCHAR(40) NOT NULL,
"position" INT NOT NULL
);

CREATE TABLE "uni1"."course" (
"c_id" INT NOT NULL PRIMARY KEY,
"title" VARCHAR(100) NOT NULL
);

CREATE TABLE "uni1"."teaching" (
"c_id" INT NOT NULL,
"a_id" INT NOT NULL
);

ALTER TABLE "uni1"."teaching"
ADD FOREIGN KEY ("c_id") REFERENCES "uni1"."course"("c_id");

ALTER TABLE "uni1"."teaching"
ADD FOREIGN KEY ("a_id") REFERENCES "uni1"."academic"("a_id");


CREATE SCHEMA "uni2";

CREATE TABLE "uni2"."person" (
"pid" INT NOT NULL PRIMARY KEY,
"pssn" VARCHAR(40) UNIQUE,
"fname" VARCHAR(40) NOT NULL,
"lname" VARCHAR(40) NOT NULL,
"status" INT NOT NULL
);

CREATE TABLE "uni2"."course" (
"cid" INT NOT NULL PRIMARY KEY,
"lecturer" INT NOT NULL,
"lab_teacher" INT NOT NULL,
"topic" VARCHAR(100) NOT NULL
);

ALTER TABLE "uni2"."course"
ADD FOREIGN KEY ("lecturer") REFERENCES "uni2"."person"("pid");

ALTER TABLE "uni2"."course"
ADD FOREIGN KEY ("lab_teacher") REFERENCES "uni2"."person"("pid");


INSERT INTO "uni1"."student"
("s_id", "s_ssn", "first_name","last_name") VALUES
(1, 'mth89', 'Mary','Smith'),
(2, 'joe83', 'John','Doe');

INSERT INTO "uni1"."academic"
("a_id","a_ssn" ,"first_name","last_name", "position") VALUES
(1, 'ars73' , 'Anna','Chambers', 1),
(2, 'eay75', 'Edward','May', 9),
(3, 'rrd70', 'Rachel', 'Ward', 8);

INSERT INTO "uni1"."course" ("c_id", "title") VALUES
(1234, 'Linear Algebra');

INSERT INTO "uni1"."teaching" ("c_id", "a_id") VALUES
(1234, 1),
(1234, 2);

INSERT INTO "uni2"."person"
("pid", "pssn", "fname", "lname", "status") VALUES
(1, 'zne69' , 'Zak', 'Lane', 8),
(2, 'mes78', 'Mattie', 'Moses', 1),
(3, 'cez85', 'Céline', 'Mendez', 2),
(4, 'eay75', 'Edward','May', 8),
(5, 'rrd70', 'Rachel', 'Ward', 8),
(6, 'mth89', 'Mary','Smith', 2);

INSERT INTO "uni2"."course"
("cid", "lecturer", "lab_teacher", "topic") VALUES
(1, 1, 3, 'Information security');
