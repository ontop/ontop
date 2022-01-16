create table "course" (
id integer not null primary key
);

create table "course-registration" (
  id integer not null,
  course_id integer not null references "course"(id)
);