CREATE DATABASE BOOKS;
CONNECT TO BOOKS;

CREATE TABLE books (
    id INTEGER NOT NULL,
	title VARCHAR(100) WITH DEFAULT NULL,
	price DECIMAL(8,2) WITH DEFAULT NULL,
	discount DECIMAL(8,2) WITH DEFAULT NULL,
	description VARCHAR(100) WITH DEFAULT NULL,
	lang VARCHAR(100) WITH DEFAULT NULL,
	publication_date TIMESTAMP
   ) ;


INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('1','SPARQL Tutorial','42.5','0.2','good','en',to_timestamp('05-JUN-14 16:47:52','DD-MON-RR HH24:MI:SS'));
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('2','The Semantic Web','23','0.25','bad','en',to_timestamp('08-DEC-11 11:30:00','DD-MON-RR HH24:MI:SS'));
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('3','Crime and Punishment','33.5','0.2','good','en',to_timestamp('21-SEP-15 09:23:06','DD-MON-RR HH24:MI:SS'));
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('4','The Logic Book: Introduction, Second Edition','10','0.15','good','en',to_timestamp('05-NOV-70 07:50:00','DD-MON-RR HH24:MI:SS'));

ALTER TABLE books ADD PRIMARY KEY (id);

COMMIT WORK;

CONNECT RESET;

TERMINATE;