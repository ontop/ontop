
CREATE TABLE books (
    id int NOT NULL,
    title character varying(100),
    price int,
    discount decimal,
    description character varying(100),
    lang character varying(100)
);


INSERT INTO books VALUES (1, 'SPARQL Tutorial', 42, 0.2, 'good', 'en' );
INSERT INTO books VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en' );

ALTER TABLE books
    ADD CONSTRAINT books_pkey PRIMARY KEY (id);


