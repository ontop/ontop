CREATE TABLE books.books (
  id int NOT NULL,
  title varchar(100) DEFAULT NULL,
  price decimal(10,2) DEFAULT NULL,
  discount decimal(10,2) DEFAULT NULL,
  description varchar(100) DEFAULT NULL,
  lang varchar(100) DEFAULT NULL,
  publication_date timestamp DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO books.books VALUES (1,'SPARQL Tutorial', 43, 0.2, 'good', 'en', '2014-06-05 16:47:52');
INSERT INTO books.books VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', '2011-12-08 11:30:00');
INSERT INTO books.books VALUES (3, 'Crime and Punishment', 34, 0.2, 'good', 'en', '2015-09-21 09:23:06');
INSERT INTO books.books VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', '1970-11-05 07:50:00');