create table "employee" (
                          "id" int primary key,
                          "firstName" varchar(100) NOT NULL,
                          "lastName" varchar(100) NOT NULL,
                          "status" int NOT NULL,
                          "country" varchar(100) NULL,
                          "locality" varchar(100) NOT NULL,
                          "role" varchar(100) NOT NULL
);

insert into "employee" ("id", "firstName", "lastName", "status", "country", "locality", "role") values (1, 'Roger','Smith', 1, 'it', 'Bozen', 'developer');
insert into "employee" ("id", "firstName", "lastName", "status", "country", "locality", "role") values (2, 'Anna','Gross', 3, 'de', 'Munich', 'sales');

create table "country" (
                           "name" varchar(100) primary key,
                           "acronym" varchar (100) UNIQUE NOT NULL,
                           "continent" varchar(100) NOT NULL
);

insert into "country" ("name", "acronym", "continent") values ('Italy', 'it', 'Europe');
insert into "country" ("name", "acronym", "continent") values ('Germany', 'de', 'Europe');