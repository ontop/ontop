CREATE DATABASE geospatial;

\connect geospatial

CREATE EXTENSION postgis;

CREATE TABLE "GEOMS" ("id" INT PRIMARY KEY, "geom" GEOMETRY, "name" TEXT);
INSERT INTO "GEOMS" VALUES (1, 'POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))', 'small rectangle'),
                           (2, 'POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))', 'large rectangle'),
                           (3, 'POINT(2.2945 48.8584)', 'Eiffel Tower'),
                           (4, 'POINT(-0.0754 51.5055)', 'Tower Bridge'),
                           (5, 'LINESTRING(1 2, 2 2, 3 2)', 'short horizontal line'),
                           (6, 'LINESTRING(1 2, 10 2)', 'long horizontal line');

CREATE TABLE "GEOGS" ("id" INT PRIMARY KEY, "geog" GEOGRAPHY, "name" TEXT);
INSERT INTO "GEOGS" VALUES (1, 'POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))', 'small rectangle'),
                           (2, 'POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))', 'large rectangle'),
                           (3, 'POINT(2.2945 48.8584)', 'Eiffel Tower'),
                           (4, 'POINT(-0.0754 51.5055)', 'Tower Bridge'),
                           (5, 'LINESTRING(1 2, 2 2, 3 2)', 'short horizontal line'),
                           (6, 'LINESTRING(1 2, 10 2)', 'long horizontal line');

CREATE TABLE "NEWGEOMS" ("id" INT PRIMARY KEY, "geom" GEOMETRY, "name" TEXT);
INSERT INTO "NEWGEOMS" VALUES (11, 'POINT(2.2942 48.8583)', 'Near Eiffel Tower');
