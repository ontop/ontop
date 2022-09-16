CREATE TABLE "profileapp1" (
"id" INT NOT NULL PRIMARY KEY,
"name" VARCHAR(40)
);

CREATE TABLE "profileapp2" (
 "id" INT NOT NULL PRIMARY KEY,
  "name" VARCHAR(40)
);

INSERT INTO "profileapp1"
("id","name") VALUES
(1, 'John');

INSERT INTO "profileapp2"
("id","name") VALUES
(1, 'John'),
(2, 'Roger');