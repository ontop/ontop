CREATE TABLE "person" (
"id" INT NOT NULL PRIMARY KEY,
"first_name" VARCHAR(40),
"last_name" VARCHAR(40),
"age" INT,
"spouse" INT
);

INSERT INTO "person"
("id","first_name","last_name", "age", "spouse") VALUES
(1,'Mary','Smith', 32, 2),
(2,'John','Doe', 31, 1),
(3, 'Bob', 'Forester', 35, null);

ALTER TABLE "person"
ADD FOREIGN KEY ("spouse") REFERENCES "person"("id");