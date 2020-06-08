CREATE TABLE "GEOMS" (id INT PRIMARY KEY , the_geom geometry, name TEXT);
INSERT INTO "GEOMS" VALUES (1, 'POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))', 'small rectangle');
INSERT INTO "GEOMS" VALUES (2, 'POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))', 'large rectangle');
INSERT INTO "GEOMS" VALUES (3, 'POINT(2.2945 48.8584)', 'Eiffel Tower');
INSERT INTO "GEOMS" VALUES (4, 'POINT(-0.0754 51.5055)', 'Tower Bridge');
