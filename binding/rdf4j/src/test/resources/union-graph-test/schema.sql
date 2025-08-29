create table "persons_corp" (
    "id" int primary key,
    "name" varchar(100) NOT NULL,
    "role" varchar(100)
);

create table "persons_academic" (
    "id" int primary key,
    "name" varchar(100) NOT NULL,
    "role" varchar(100)
);

create table "persons_research" (
    "id" int primary key,
    "name" varchar(100) NOT NULL,
    "role" varchar(100)
);

insert into "persons_corp" ("id", "name", "role") values (1, 'John Smith', 'developer');
insert into "persons_corp" ("id", "name", "role") values (2, 'Jane Doe', 'manager');

insert into "persons_academic" ("id", "name", "role") values (3, 'Prof. Wilson', 'professor');
insert into "persons_academic" ("id", "name", "role") values (5, 'Alice Johnson', 'student');

insert into "persons_research" ("id", "name", "role") values (4, 'Dr. Brown', 'researcher');
