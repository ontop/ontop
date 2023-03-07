DROP TABLE IF EXISTS university.professors;
DROP TABLE IF EXISTS university.course;
DROP TABLE IF EXISTS university.teaching;

CREATE TABLE university.professors (
    prof_id int not null,
	first_name string(100) NOT NULL,
	last_name string(100) NOT NULL,
    nickname string(100)
) AS SELECT * FROM (
    (SELECT 1, 'Roger', 'Smith', 'Rog') UNION ALL
    (SELECT 2, 'Frank', 'Pitt', 'Frankie') UNION ALL
    (SELECT 3, 'John', 'Depp', 'Johnny') UNION ALL
    (SELECT 4, 'Michael', 'Jackson', 'King of Pop') UNION ALL
    (SELECT 5, 'Diego', 'Gamper', null) UNION ALL
    (SELECT 6, 'Johann', 'Helmer', null) UNION ALL
    (SELECT 7, 'Barbara', 'Dodero', null) UNION ALL
    (SELECT 8, 'Mary', 'Poppins', null)
);

CREATE TABLE university.course (
	course_id STRING(100) NOT NULL,
	nb_students int NOT NULL,
	duration numeric NOT NULL
) AS SELECT * FROM (
    (SELECT 'LinearAlgebra', 10, NUMERIC '24.5') UNION ALL
    (SELECT 'DiscreteMathematics', 11, NUMERIC '30') UNION ALL
    (SELECT 'AdvancedDatabases', 12, NUMERIC '20') UNION ALL
    (SELECT 'ScientificWriting', 13, NUMERIC '18') UNION ALL
    (SELECT 'OperatingSystems', 10, NUMERIC '30')
);

CREATE TABLE university.teaching (
	course_id STRING(100) NOT NULL,
	prof_id int NOT NULL,
) AS SELECT * FROM (
    (SELECT 'LinearAlgebra', 1) UNION ALL
    (SELECT 'DiscreteMathematics', 1) UNION ALL
    (SELECT 'AdvancedDatabases', 3) UNION ALL
    (SELECT 'ScientificWriting', 8) UNION ALL
    (SELECT 'OperatingSystems', 1)
);