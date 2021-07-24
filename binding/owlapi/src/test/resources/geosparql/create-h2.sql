CREATE TABLE "GEOMS" (id INT PRIMARY KEY , the_geom geometry, name TEXT);
INSERT INTO "GEOMS" VALUES (1, 'POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))', 'small rectangle');
INSERT INTO "GEOMS" VALUES (2, 'POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))', 'large rectangle');
INSERT INTO "GEOMS" VALUES (3, 'POINT(2.2945 48.8584)', 'Eiffel Tower');
INSERT INTO "GEOMS" VALUES (4, 'POINT(-0.0754 51.5055)', 'Tower Bridge');
INSERT INTO "GEOMS" VALUES (5, 'POLYGON((3 3, 8 3, 8 6, 3 6, 3 3))', 'small rectangle 2');
INSERT INTO "GEOMS" VALUES (6, 'POLYGON((1 1, 8 1, 8 7, 1 7, 1 1))', 'large rectangle 2');
INSERT INTO "GEOMS" VALUES (7, 'POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 'very small rectangle');
INSERT INTO "GEOMS" VALUES (8, 'POLYGON((1 2, 2 2, 2 3, 1 3, 1 2))', 'small rectangle 3');
INSERT INTO "GEOMS" VALUES (9, 'LINESTRING(1 2, 2 2, 3 2)', 'short horizontal line');
INSERT INTO "GEOMS" VALUES (10, 'LINESTRING(1 2, 10 2)', 'long horizontal line');
INSERT INTO "GEOMS" VALUES (11, 'POINT(2 2)', 'point');
INSERT INTO "GEOMS" VALUES (12, 'LINESTRING(2 1, 2 10)', 'long vertical line');
INSERT INTO "GEOMS" VALUES (13, 'POLYGON((0 0, 3 0, 3 3, 0 3, 0 0))', 'intersecting polygon');
INSERT INTO "GEOMS" VALUES (14, 'POLYGON((0 3, 3 3, 3 6, 0 6, 0 3))', 'intersecting polygon');
INSERT INTO "GEOMS" VALUES (15, 'LINESTRING(2 2, 11 2)', 'long horizontal line');
INSERT INTO "GEOMS" VALUES (16, 'POLYGON((2 2, 6 2, 4 4, 2 2))', 'triangle');
INSERT INTO "GEOMS" VALUES (17, 'GEOMETRYCOLLECTION(POLYGON((2 2, 6 2, 4 4, 2 2)), POINT(7 7))', 'polygon + point');
INSERT INTO "GEOMS" VALUES (18, 'POLYGON EMPTY', 'empty');
INSERT INTO "GEOMS" VALUES (19, 'GEOMETRYCOLLECTION(POLYGON((2 2, 6 2, 4 4, 2 2)), POLYGON((0 3, 3 3, 3 6, 0 6, 0 3)))', 'polygon + polygon');
INSERT INTO "GEOMS" VALUES (20, 'POLYGON ((0 2, 0 6, 6 6, 6 2, 0 2))', 'Not Simple Geom');
INSERT INTO "GEOMS" VALUES (21, 'POINT(668682.853 5122639.964)', 'a point in BZ with SRID <http://www.opengis.net/def/crs/EPSG/0/3044>');
INSERT INTO "GEOMS" VALUES (22, 'POINT(2.2945 48.8584)', 'Eiffel Tower <http://www.opengis.net/def/crs/OGC/1.3/CRS84>');
INSERT INTO "GEOMS" VALUES (23, 'POINT(-0.0754 51.5055)', 'Tower Bridge <http://www.opengis.net/def/crs/OGC/1.3/CRS84>');
INSERT INTO "GEOMS" VALUES (24, 'POINT(2.2945 48.8584)', 'Eiffel Tower <http://www.opengis.net/def/crs/EPSG/0/4326>');
INSERT INTO "GEOMS" VALUES (25, 'POINT(-0.0754 51.5055)', 'Tower Bridge <http://www.opengis.net/def/crs/EPSG/0/4326>');
INSERT INTO "GEOMS" VALUES (26, 'POINT(668683.853 5122640.964)', 'a point in BZ with SRID <http://www.opengis.net/def/crs/EPSG/0/3044>');


CREATE TABLE "POINTS" (id INT PRIMARY KEY , longitude float, latitude float, name TEXT);
INSERT INTO "POINTS" VALUES (3, 2.2945, 48.8584, 'Eiffel Tower');
INSERT INTO "POINTS" VALUES (4, -0.0754, 51.5055, 'Tower Bridge');
INSERT INTO "POINTS" VALUES (21, 668682.853, 5122639.964, 'a point in BZ with SRID <http://www.opengis.net/def/crs/EPSG/0/3044>');
INSERT INTO "POINTS" VALUES (26, 668683.853, 5122640.964, 'a point in BZ with SRID <http://www.opengis.net/def/crs/EPSG/0/3044>');


CREATE TABLE "FEATURES" (id INT PRIMARY KEY, gid TEXT, the_geom geometry, name TEXT);
INSERT INTO "FEATURES" VALUES (1, 'FRANCE1', 'POINT(2.2945 48.8584)', 'Eiffel Tower');
INSERT INTO "FEATURES" VALUES (2, 'UK1', 'POINT(-0.0754 51.5055)', 'Tower Bridge');


CREATE TABLE "RIVERS" (id INT, lat float, lon float);
INSERT INTO "RIVERS" VALUES (1, 2.2945, 48.8584);
INSERT INTO "RIVERS" VALUES (2, -0.0754, 51.5055);


CREATE TABLE "LAKES" (id INT, lat float, lon float);
INSERT INTO "LAKES" VALUES (1, 2.3945, 49.8584);
INSERT INTO "LAKES" VALUES (2, -0.1754, 52.5055);