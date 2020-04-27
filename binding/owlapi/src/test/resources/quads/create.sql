DROP TABLE IF EXISTS Job;
CREATE TABLE Job (
  idJob int(11) NOT NULL,
  description varchar(45),
  PRIMARY KEY (idJob)
) DEFAULT CHARSET=utf8;

INSERT INTO Job VALUES (1,'Job 1'),(2,'Job 2'),(3,'Job 3');

DROP TABLE IF EXISTS Person;
CREATE TABLE Person (
  idPerson int(11) NOT NULL,
  name varchar(45),
  birthYear int(11),
  idJob int(11),
  PRIMARY KEY (idPerson),
) DEFAULT CHARSET=utf8;

INSERT INTO Person VALUES (1,'Person 1',1989,1),(2,'Person 2',1970,1),(3,'Person 3',1975,2),(4,'Person 4',1992,3);
