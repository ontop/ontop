create table "person" (
	"id" int primary key,
	"firstName" varchar(100),
	"lastName" varchar(100),
	"fullName" varchar(100) NOT NULL,
	"status" int NOT NULL,
	"country" varchar(100) NOT NULL,
	"locality" varchar(100)
);

insert into "person" ("id", "firstName", "lastName", "fullName", "status", "country", "locality") values (1, 'Roger', 'Smith', 'Roger Smith', 1, 'it', NULL);
insert into "person" ("id", "firstName", "lastName", "fullName", "status", "country", "locality") values (2, 'John', NULL, 'John Doe', 0, 'uk', 'Belfast');
insert into "person" ("id", "firstName", "lastName", "fullName", "status", "country", "locality") values (3, NULL, 'Doe', 'Jane Doe', 0, 'ie', 'Dublin');
insert into "person" ("id", "firstName", "lastName", "fullName", "status", "country", "locality") values (4, NULL, NULL, 'Corto Maltese', 0, 'mt', 'Valletta');

create table "statuses" (
    "status_id" int primary key,
    "description" varchar(100) NOT NULL
);

insert into "statuses" ("status_id", "description") values (1, 'OK');

