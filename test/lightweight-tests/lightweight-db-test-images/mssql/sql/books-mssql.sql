CREATE DATABASE [books];
GO
USE [books];
GO

CREATE TABLE books (
    id int NOT NULL,
	title varchar(100),
	price decimal(8,2),
	discount decimal(8,2),
	description varchar(100),
	lang varchar(100),
	publication_date datetime
   ) ;

ALTER TABLE books
    ADD CONSTRAINT books_pkey PRIMARY KEY (ID);

INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('1','SPARQL Tutorial','43',CAST('0.2' AS Decimal (8,2)),'good','en', CAST('05-JUN-14 16:47:52' AS DateTime));
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('2','The Semantic Web','23',CAST('0.25' AS Decimal (8,2)),'bad','en', CAST('08-DEC-11 11:30:00' AS DateTime));
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('3','Crime and Punishment','34',CAST('0.2' AS Decimal (8,2)),'good','en', CAST('21-SEP-15 09:23:06' AS DateTime));
INSERT INTO books (id, title, price, discount, description, lang, publication_date) VALUES ('4','The Logic Book: Introduction, Second Edition','10',CAST('0.15' AS Decimal (8,2)),'good','en', CAST('05-NOV-70 07:50:00'AS DateTime));
