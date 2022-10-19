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

create table "activity_denorm" (
                            "employeeId" int not null,
                            "type" int not null,
                            "title" varchar(100) not null,
                            foreign key ("employeeId") references "employee"("id")
);

insert into "activity_denorm" ("employeeId", "type", "title") values (1, 1, 'Deployment on Kubernetes');
insert into "activity_denorm" ("employeeId", "type", "title") values (2, 2, 'CRM update');

