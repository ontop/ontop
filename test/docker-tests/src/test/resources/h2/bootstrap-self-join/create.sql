create table "course" (
id integer not null primary key
);

create table "course-registration" (
  id integer not null primary key,
  course_id integer not null references "course"(id),
  course_prerequisite integer null references "course-registration" (id)
);