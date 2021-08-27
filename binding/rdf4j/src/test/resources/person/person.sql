create table "person" (
                          "id" int primary key,
                          "fullName" varchar(100) NOT NULL,
                          "status" int NOT NULL,
                          "country" varchar(100) NOT NULL,
                          "locality" varchar(100) NOT NULL
);

insert into "person" ("id", "fullName", "status", "country", "locality") values (1, 'Roger Smith', 1, 'it', 'Botzen');

create table "country" (
                           "name" varchar(100) primary key,
                           "acronym" varchar (100) UNIQUE NOT NULL,
                           "continent" varchar(100) NOT NULL
);

insert into "country" ("name", "acronym", "continent") values ('Italy', 'it', 'Europe');
insert into "country" ("name", "acronym", "continent") values ('Germany', 'de', 'Europe');