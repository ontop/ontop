create table "person" (
                          "id" int primary key,
                          "firstName" varchar(100),
                          "lastName" varchar(100),
                          "nickname" varchar(100),
                          "status" int NOT NULL,
                          "country" varchar(100) NOT NULL,
                          "locality" varchar(100)
);

insert into "person" ("id", "firstName", "lastName", "nickname", "status", "country", "locality") values (1, 'Roger', 'Smith', 'Roger Smith', 1, 'it', NULL);
insert into "person" ("id", "firstName", "lastName", "nickname", "status", "country", "locality") values (2, 'John', NULL, 'John',0, 'uk', 'Belfast');
insert into "person" ("id", "firstName", "lastName", "nickname", "status", "country", "locality") values (3, NULL, 'Doe', 'Jane Doe', 0, 'ie', 'Dublin');
insert into "person" ("id", "firstName", "lastName", "nickname", "status", "country", "locality") values (4, NULL, NULL, 'Corto Maltese', 0, 'mt', 'Valletta');
insert into "person" ("id", "firstName", "lastName", "nickname", "status", "country", "locality") values (5,  'Roger', NULL, 'Rog', 0, 'it', 'Rome');
