create table professors (
	prof_id int,
	last_name varchar(100),
	primary key (prof_id)
);

insert into professors (prof_id, last_name) values (1, 'aaa');
insert into professors (prof_id, last_name) values (2, 'bbb');
insert into professors (prof_id, last_name) values (3, 'ccc');
insert into professors (prof_id, last_name) values (4, 'ddd');
insert into professors (prof_id, last_name) values (5, 'eee');
insert into professors (prof_id, last_name) values (6, 'fff');
insert into professors (prof_id, last_name) values (7, 'ggg');
insert into professors (prof_id, last_name) values (8, 'hhh');

create table teaching (
	course_id int,
	prof_id int,
	foreign key (prof_id) REFERENCES professors(prof_id)
);

insert into teaching (course_id, prof_id) values (1, 1);
insert into teaching (course_id, prof_id) values (2, 1);
insert into teaching (course_id, prof_id) values (3, 3);
insert into teaching (course_id, prof_id) values (4, 8);