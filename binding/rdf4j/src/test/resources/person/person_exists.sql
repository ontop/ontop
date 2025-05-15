create table "person" (
                          "id" int primary key,
                          "firstName" varchar(100),
                          "lastName" varchar(100),
                          "nickname" varchar(100),
                          "moniker" varchar(100),
                          "country" varchar(100) NOT NULL,
                          "locality" varchar(100)
);

insert into "person" ("id", "firstName", "lastName", "nickname", "moniker", "country", "locality") values (1, 'Roger', 'Smith', 'Roger Smith', NULL, 'it', NULL);
insert into "person" ("id", "firstName", "lastName", "nickname", "moniker", "country", "locality") values (2, 'John', NULL, 'John',NULL, 'uk', 'Belfast');
insert into "person" ("id", "firstName", "lastName", "nickname", "moniker", "country", "locality") values (3, NULL, 'Doe', NULL, NULL, 'ie', 'Dublin');
insert into "person" ("id", "firstName", "lastName", "nickname", "moniker", "country", "locality") values (4, NULL, NULL, 'Corto Maltese', NULL, 'mt', 'Valletta');
insert into "person" ("id", "firstName", "lastName", "nickname", "moniker", "country", "locality") values (5,  'Roger', NULL, 'Rog', NULL, 'it', 'Rome');
insert into "person" ("id", "firstName", "lastName", "nickname", "moniker", "country", "locality") values (6,  NULL, NULL, 'Spencer', 'Spence', 'it', 'Rome');
