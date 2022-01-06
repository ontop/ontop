DROP SCHEMA IF EXISTS "uni1";
DROP SCHEMA IF EXISTS "uni2";

CREATE SCHEMA "uni1";

CREATE TABLE "uni1"."student" (
                                  "s_id" INT NOT NULL PRIMARY KEY,
                                  "first_name" VARCHAR(40) NOT NULL,
                                  "last_name" VARCHAR(40) NOT NULL
);

INSERT INTO "uni1"."student"
("s_id","first_name","last_name") VALUES
                                      (1,'Mary','Smith'),
                                      (2,'John','Doe'),
                                      (3, 'Franck', 'Combs'),
                                      (4, 'Billy', 'Hinkley'),
                                      (5, 'Alison', 'Robards');
