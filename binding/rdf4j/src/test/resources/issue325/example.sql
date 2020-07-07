DROP TABLE IF EXISTS example1;
CREATE TABLE example1 (`primarykey` INT,`id` INT, PRIMARY KEY (id));

DROP TABLE IF EXISTS example2;
CREATE TABLE example2 (`id` INT, PRIMARY KEY (id));

ALTER TABLE example1 ADD FOREIGN KEY (id) REFERENCES example2 (id);

INSERT INTO example2 (id) VALUES (1), (2), (3);
INSERT INTO example1 (primarykey,id) VALUES (1,1), (1,2), (1,3);
