CREATE TABLE books (
    id int NOT NULL,
    title character varying(100),
    price int,
    discount decimal,
    description character varying(100),
    lang character varying(100),
    publication_date TIMESTAMP
);


INSERT INTO books VALUES (1, 'SPARQL Tutorial', 42.50, 0.2, 'good', 'en', parsedatetime('2014-06-05 18:47:52' , 'yyyy-MM-dd hh:mm:ss') );
INSERT INTO books VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', '2011-12-08' );
INSERT INTO books VALUES (3, 'Crime and Punishment', 33.50, 0.2, 'good', 'en', '1866-07-01' );
INSERT INTO books VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', '1967-11-05' );

ALTER TABLE books
    ADD CONSTRAINT books_pkey PRIMARY KEY (id);
