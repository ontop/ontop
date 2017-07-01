CREATE TABLE tbl_patient ( 
	patientid INT NOT NULL PRIMARY KEY, -- a numeric id for the patient
	name VARCHAR(40),  -- name of the patient
	type BOOLEAN,  -- cancer type 
				-- false = non-small cell lung cancer (NSCLC), 
				-- true = small cell lung cancer (SCLC)
	stage TINYINT -- cancer stages, 1-6 are for NSCLC,  7-8 for NSCLC
);

INSERT INTO tbl_patient (patientid,name,type,stage) 
VALUES 
(1,'Mary',false,2),
(2,'John',true,7);


CREATE TABLE t_name ( 
	id INT NOT NULL PRIMARY KEY, -- a numeric id for the patient
	name VARCHAR(40),  -- name of the patient
);

CREATE TABLE t_nsclc ( 
	id INT NOT NULL PRIMARY KEY, -- a numeric id for the patient
    stage VARCHAR(10) 
);

CREATE TABLE t_sclc ( 
	id INT NOT NULL PRIMARY KEY, -- a numeric id for the patient
    stage VARCHAR(10) -- cancer stages, 1-6 are for NSCLC,  7-8 for NSCLC
);


INSERT INTO t_name (id,name) 
VALUES 
(1,'Mariano'),
(2,'Morgan');

INSERT INTO t_nsclc (id,stage) 
VALUES 
(1,'i');

INSERT INTO t_sclc (id,stage) 
VALUES 
(2,'limited');

