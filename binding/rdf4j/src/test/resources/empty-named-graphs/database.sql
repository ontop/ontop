CREATE TABLE "person_a" (
    "id" INT PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL
);
INSERT INTO "person_a" ("id", "name") VALUES (1, 'Alice');

CREATE TABLE "person_b" (
    "id" INT PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL
);
INSERT INTO "person_b" ("id", "name") VALUES (2, 'Bob');
INSERT INTO "person_b" ("id", "name") VALUES (3, 'Charlie');

CREATE TABLE "person_c" (
    "id" INT PRIMARY KEY,
    "name" VARCHAR(100)
);
INSERT INTO "person_c" ("id", "name") VALUES (4, 'David');
INSERT INTO "person_c" ("id", "name") VALUES (5, null);