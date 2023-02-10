CREATE TABLE university.professors (
	prof_id int PRIMARY KEY,
	first_name varchar(100) NOT NULL,
	last_name varchar(100) NOT NULL,
    nickname varchar(100)
);

INSERT INTO university.professors (prof_id, first_name, last_name, nickname) VALUES (1, 'Roger', 'Smith', 'Rog');
INSERT INTO university.professors (prof_id, first_name, last_name, nickname) VALUES (2, 'Frank', 'Pitt', 'Frankie');
INSERT INTO university.professors (prof_id, first_name, last_name, nickname) VALUES (3, 'John', 'Depp', 'Johnny');
INSERT INTO university.professors (prof_id, first_name, last_name, nickname) VALUES (4, 'Michael', 'Jackson', 'King of Pop');
INSERT INTO university.professors (prof_id, first_name, last_name) VALUES (5, 'Diego', 'Gamper');
INSERT INTO university.professors (prof_id, first_name, last_name) VALUES (6, 'Johann', 'Helmer');
INSERT INTO university.professors (prof_id, first_name, last_name) VALUES (7, 'Barbara', 'Dodero');
INSERT INTO university.professors (prof_id, first_name, last_name) VALUES (8, 'Mary', 'Poppins');

CREATE TABLE university.course (
	course_id varchar(100) PRIMARY KEY,
	nb_students int NOT NULL,
	duration decimal(10, 3) NOT NULL
);

INSERT INTO university.course (course_id, nb_students, duration) VALUES ('LinearAlgebra', 10, 24.5);
INSERT INTO university.course (course_id, nb_students, duration) VALUES ('DiscreteMathematics', 11, 30);
INSERT INTO university.course (course_id, nb_students, duration) VALUES ('AdvancedDatabases', 12, 20);
INSERT INTO university.course (course_id, nb_students, duration) VALUES ('ScientificWriting', 13, 18);
INSERT INTO university.course (course_id, nb_students, duration) VALUES ('OperatingSystems', 10, 30);

create table university.teaching (
	course_id varchar(100) NOT NULL,
	prof_id int NOT NULL,
	PRIMARY KEY (course_id, prof_id),
	FOREIGN KEY (prof_id) REFERENCES university.professors(prof_id),
	FOREIGN KEY (course_id) REFERENCES university.course(course_id)
);

INSERT INTO university.teaching (course_id, prof_id) VALUES ('LinearAlgebra', 1);
INSERT INTO university.teaching (course_id, prof_id) VALUES ('DiscreteMathematics', 1);
INSERT INTO university.teaching (course_id, prof_id) VALUES ('AdvancedDatabases', 3);
INSERT INTO university.teaching (course_id, prof_id) VALUES ('ScientificWriting', 8);
INSERT INTO university.teaching (course_id, prof_id) VALUES ('OperatingSystems', 1);
