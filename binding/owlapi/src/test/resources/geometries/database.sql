create table "geometry" (
    "id" integer primary key,
    "name" text,
    "geom" geometry
);

insert into "geometry" ("id", "name", "geom") values (1, 'First point', 'POINT(11.4128 47.2187)');
insert into "geometry" ("id", "name", "geom") values (2, 'Second point', 'POINT(10.2638 45.6628)');