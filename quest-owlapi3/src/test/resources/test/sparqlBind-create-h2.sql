
CREATE TABLE books (
    id int NOT NULL,
    title character varying(100),
    price int,
    discount decimal
);


INSERT INTO books VALUES (1, 'SPARQL Tutorial', 42, 0.2 );
INSERT INTO books VALUES (2, 'The Semantic Web', 23, 0.25 );

ALTER TABLE books
    ADD CONSTRAINT books_pkey PRIMARY KEY (id);


