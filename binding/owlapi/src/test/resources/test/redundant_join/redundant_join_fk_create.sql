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

create table teaching (
	course_id varchar(100) NOT NULL,
	prof_id int NOT NULL,
	foreign key (prof_id) REFERENCES professors(prof_id)
);

insert into teaching (course_id, prof_id) values ('LinearAlgebra', 1);
insert into teaching (course_id, prof_id) values ('DiscreteMathematics', 1);
insert into teaching (course_id, prof_id) values ('AdvancedDatabases', 3);
insert into teaching (course_id, prof_id) values ('ScientificWriting', 8);