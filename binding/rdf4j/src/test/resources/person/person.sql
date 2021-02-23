create table "person" (
	"id" int primary key,
	"fullName" varchar(100) NOT NULL,
	"status" int NOT NULL,
	"country" varchar(100) NOT NULL,
	"locality" varchar(100) NOT NULL
);

insert into "person" ("id", "fullName", "status", "country", "locality") values (1, 'Roger Smith', 1, 'it', 'Botzen');

create table "statuses" (
    "status_id" int primary key,
    "description" varchar(100) NOT NULL
);

insert into "statuses" ("status_id", "description") values (1, 'OK');

