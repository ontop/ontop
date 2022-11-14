CREATE TABLE "SYSTEM".professors (
	prof_id NUMBER(9) PRIMARY KEY,
	first_name VARCHAR2(100) NOT NULL,
	last_name VARCHAR2(100) NOT NULL,
    nickname VARCHAR2(100)
);

INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name, nickname) VALUES (1, 'Roger', 'Smith', 'Rog');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name, nickname) VALUES (2, 'Frank', 'Pitt', 'Frankie');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name, nickname) VALUES (3, 'John', 'Depp', 'Johnny');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name, nickname) VALUES (4, 'Michael', 'Jackson', 'King of Pop');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name) VALUES (5, 'Diego', 'Gamper');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name) VALUES (6, 'Johann', 'Helmer');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name) VALUES (7, 'Barbara', 'Dodero');
INSERT INTO "SYSTEM".professors (prof_id, first_name, last_name) VALUES (8, 'Mary', 'Poppins');

create table "SYSTEM".course (
	course_id VARCHAR2(100) PRIMARY KEY,
	nb_students NUMBER(9) NOT NULL,
	duration decimal (10, 3) NOT NULL
);

INSERT INTO "SYSTEM".course (course_id, nb_students, duration) VALUES ('LinearAlgebra', 10, 24.5);
INSERT INTO "SYSTEM".course (course_id, nb_students, duration) VALUES ('DiscreteMathematics', 11, 30);
INSERT INTO "SYSTEM".course (course_id, nb_students, duration) VALUES ('AdvancedDatabases', 12, 20);
INSERT INTO "SYSTEM".course (course_id, nb_students, duration) VALUES ('ScientificWriting', 13, 18);
INSERT INTO "SYSTEM".course (course_id, nb_students, duration) VALUES ('OperatingSystems', 10, 30);

CREATE TABLE "SYSTEM".teaching (
	course_id VARCHAR2(100) NOT NULL,
	prof_id NUMBER(9) NOT NULL,
	PRIMARY KEY (course_id, prof_id),
	FOREIGN KEY (prof_id) REFERENCES "SYSTEM".professors(prof_id),
	FOREIGN KEY (course_id) REFERENCES "SYSTEM".course(course_id)
);

INSERT INTO "SYSTEM".teaching (course_id, prof_id) VALUES ('LinearAlgebra', 1);
INSERT INTO "SYSTEM".teaching (course_id, prof_id) VALUES ('DiscreteMathematics', 1);
INSERT INTO "SYSTEM".teaching (course_id, prof_id) VALUES ('AdvancedDatabases', 3);
INSERT INTO "SYSTEM".teaching (course_id, prof_id) VALUES ('ScientificWriting', 8);
INSERT INTO "SYSTEM".teaching (course_id, prof_id) VALUES ('OperatingSystems', 1);
