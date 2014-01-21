CREATE TABLE "COUNTRIES" (
	"primary_key" INT,
	PRIMARY KEY("primary_key"),
	"COUNTRY_NAME" VARCHAR(100)
);


CREATE TABLE "movies" (
	"Primary_Key" INT,
	PRIMARY KEY("Primary_Key"),
	"MOVIE_NAME" VARCHAR(100)
);

INSERT INTO COUNTRIES ("primary_key", COUNTRY_NAME)
              VALUES (1010, 'Argentina' );

             
INSERT INTO "movies" ("Primary_Key", MOVIE_NAME)
              VALUES (2020, 'BladeRunner' );
