CREATE TABLE "person" (
"id" INT NOT NULL PRIMARY KEY,
"first_name" VARCHAR(40),
"last_name" VARCHAR(40),
"spouse" INT
);

CREATE TABLE "musician" (
"mid" INT NOT NULL PRIMARY KEY,
"instrument" VARCHAR(40) NOT NULL
);

INSERT INTO "person"
("id","first_name","last_name", "spouse") VALUES
(1,'Mary','Smith', 2),
(2,'John','Doe', 1),
(3, 'Bob', 'Forester', null);


INSERT INTO "musician"
("mid","instrument") VALUES
(1, 'Piano');

ALTER TABLE "person"
ADD FOREIGN KEY ("spouse") REFERENCES "person"("id");

ALTER TABLE "musician"
ADD FOREIGN KEY ("mid") REFERENCES "person"("id");