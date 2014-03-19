CREATE TABLE people (
	p_id INT,
	PRIMARY KEY(p_id),
	p_name VARCHAR(10),
	sex VARCHAR(10)
);

CREATE TABLE family (
	f_id INT,
	PRIMARY KEY(f_id),
	p_id int,
	relative VARCHAR(50),
	relation VARCHAR(10)
);


INSERT INTO people (p_id, p_name, sex)
                 VALUES (1,   'Mark', 'M');
INSERT INTO people (p_id, p_name, sex)
                 VALUES (2,   'Amanda', 'F');
INSERT INTO family (f_id, p_id, relative, relation )
              VALUES (1,  1,   'John',  'S');
              