CREATE SCHEMA "stockexchange";

CREATE TABLE "stockexchange".test (
    name1 character varying(100),
	name2 character varying(100),
	name3 character varying(100)
);

INSERT INTO "stockexchange".test VALUES ('John Smith', 'John Smith 2', 'John Smith 3');
INSERT INTO "stockexchange".test VALUES ('Cote D''ivore', 'Cote D''ivore 2', 'Cote D''ivore 3');
-- INSERT INTO "stockexchange".test VALUES ();


-- ALTER TABLE "stockexchange".test
--     ADD CONSTRAINT test_pkey PRIMARY KEY (id);

