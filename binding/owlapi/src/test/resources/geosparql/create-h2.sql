CREATE TABLE "GEOMS" (id INT, the_geom geometry, name TEXT);
INSERT INTO "GEOMS" VALUES (1, 'POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))', 'small rectangle');
INSERT INTO "GEOMS" VALUES (2, 'POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))', 'large rectangle');
