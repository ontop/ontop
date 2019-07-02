create table professors (
	prof_id int primary key,
	first_name varchar(100) NOT NULL,
	last_name varchar(100) NOT NULL,
  nickname varchar(100)
);

insert into professors (prof_id, first_name, last_name, nickname) values (1, 'Roger', 'Smith', 'Rog');
insert into professors (prof_id, first_name, last_name, nickname) values (2, 'Frank', 'Pitt', 'Frankie');
insert into professors (prof_id, first_name, last_name, nickname) values (3, 'John', 'Depp', 'Johnny');
insert into professors (prof_id, first_name, last_name, nickname) values (4, 'Michael', 'Jackson', 'King of Pop');
insert into professors (prof_id, first_name, last_name) values (5, 'Diego', 'Gamper');
insert into professors (prof_id, first_name, last_name) values (6, 'Johann', 'Helmer');
insert into professors (prof_id, first_name, last_name) values (7, 'Barbara', 'Dodero');
insert into professors (prof_id, first_name, last_name) values (8, 'Mary', 'Poppins');

create table course (
	course_id varchar(100) primary key,
	nb_students int NOT NULL,
	duration decimal NOT NULL
);

insert into course (course_id, nb_students, duration) values ('LinearAlgebra', 10, 24.5);
insert into course (course_id, nb_students, duration) values ('DiscreteMathematics', 11, 30);
insert into course (course_id, nb_students, duration) values ('AdvancedDatabases', 12, 20);
insert into course (course_id, nb_students, duration) values ('ScientificWriting', 13, 18);

create table teaching (
	course_id varchar(100) NOT NULL,
	prof_id int NOT NULL,
	primary key (course_id, prof_id),
	foreign key (prof_id) REFERENCES professors(prof_id),
	foreign key (course_id) REFERENCES course(course_id)
);

insert into teaching (course_id, prof_id) values ('LinearAlgebra', 1);
insert into teaching (course_id, prof_id) values ('DiscreteMathematics', 1);
insert into teaching (course_id, prof_id) values ('AdvancedDatabases', 3);
insert into teaching (course_id, prof_id) values ('ScientificWriting', 8);