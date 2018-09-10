create table professors (
	prof_id int primary key,
	first_name varchar(100) NOT NULL,
	last_name varchar(100) NOT NULL,
  nickname varchar(100)
);

insert into professors (prof_id, first_name, last_name, nickname) values (1, 'Roger', 'Smith', 'Rog');
insert into professors (prof_id, first_name, last_name, nickname) values (2, 'Frank', 'Pitt', 'Frankie');

create table teaching (
	course_id varchar(100) NOT NULL,
	prof_id int NOT NULL,
	foreign key (prof_id) REFERENCES professors(prof_id)
);

insert into teaching (course_id, prof_id) values ('LinearAlgebra', 1);
insert into teaching (course_id, prof_id) values ('DiscreteMathematics', 1);
