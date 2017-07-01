CREATE TABLE "person" (
"id" INT NOT NULL PRIMARY KEY,
"first_name" VARCHAR(40),
"last_name" VARCHAR(40),
"spouse" INT
);

INSERT INTO "person"
("id","first_name","last_name", "spouse") VALUES
(1,'Mary','Smith', 2),
(2,'John','Doe', 1),
(3, 'Bob', 'Forester', null);

ALTER TABLE "person"
ADD FOREIGN KEY ("spouse") REFERENCES "person"("id");