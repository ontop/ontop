CREATE TABLE "shipping" (
"id" INT NOT NULL PRIMARY KEY,
"name" VARCHAR(40),
"sourceCountry" VARCHAR(40),
"destCountry" VARCHAR(40)
);

INSERT INTO "shipping"
("id","name","sourceCountry", "destCountry") VALUES
(1, 'computer', 'Italy','France'),
(2, 'car', 'Germany', 'Italy'),
(3, 'bike', 'Belgium', null);