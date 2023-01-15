ALTER SESSION SET container=XEPDB1;

CREATE USER books IDENTIFIED BY books_xyz container=current;
GRANT CREATE SESSION TO books;
GRANT CREATE TABLE TO books;
ALTER USER books QUOTA UNLIMITED ON users;


CREATE TABLE books.books (
                                id NUMBER(*,0) NOT NULL,
                                title VARCHAR2(100),
                                price NUMBER(8,2),
                                discount NUMBER(8,2),
                                description VARCHAR2(100),
                                lang VARCHAR2(100),
                                publication_date TIMESTAMP
);

-- Default timestamp with timezone value in Oracle is DD-MON-RR HH.MI.SSXFF AM TZR
-- Modified format used to handle these instances

INSERT INTO books.books (id, title, price, discount, description, lang, publication_date) VALUES (1, 'SPARQL Tutorial', 43, 0.2, 'good', 'en', to_timestamp('2014-06-05 16:47:52', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO books.books (id, title, price, discount, description, lang, publication_date) VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', to_timestamp('2011-12-08 11:30:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO books.books (id, title, price, discount, description, lang, publication_date) VALUES (3, 'Crime and Punishment', 34, 0.2, 'good', 'en', to_timestamp('2015-09-21 09:23:06', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO books.books (id, title, price, discount, description, lang, publication_date) VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', to_timestamp('1970-11-05 07:50:00', 'YYYY-MM-DD HH24:MI:SS'));

ALTER TABLE books.books ADD CONSTRAINT "books_pkey" PRIMARY KEY (id) ENABLE;