DROP TABLE IF EXISTS example1;
CREATE TABLE "example1" ("s" VARCHAR(100) NOT NULL, "o" VARCHAR(100) NOT NULL);

INSERT INTO "example1" ("s", "o") VALUES ('http://example.org/sub', 'http://example.org/obj');
