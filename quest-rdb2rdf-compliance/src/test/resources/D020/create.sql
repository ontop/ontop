CREATE TABLE "Student" (
"Name" VARCHAR(50)
);

INSERT INTO "Student" ("Name") VALUES ('http://example.com/company/Alice');
INSERT INTO "Student" ("Name") VALUES ('Bob');
INSERT INTO "Student" ("Name") VALUES ('Bob/Charles');
INSERT INTO "Student" ("Name") VALUES ('path/../Danny');
INSERT INTO "Student" ("Name") VALUES ('Emily Smith');