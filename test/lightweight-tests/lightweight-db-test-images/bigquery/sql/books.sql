DROP TABLE IF EXISTS books.books;
CREATE TABLE books.books (
    id integer NOT NULL,
    title STRING(100),
    price integer,
    discount NUMERIC,
    description STRING(100),
    lang STRING(100),
    publication_date timestamp
) as SELECT * from (
  (select 1, 'SPARQL Tutorial', 43, NUMERIC '0.2', 'good', 'en', TIMESTAMP '2014-06-05 16:47:52') UNION ALL
  (select 2, 'The Semantic Web', 23, NUMERIC '0.25', 'bad', 'en', TIMESTAMP '2011-12-08 11:30:00') UNION ALL
  (select 3, 'Crime and Punishment', 34, NUMERIC '0.2', 'good', 'en', TIMESTAMP '2015-09-21 09:23:06') UNION ALL
  (select 4, 'The Logic Book: Introduction, Second Edition', 10, NUMERIC '0.15', 'good', 'en', TIMESTAMP '1970-11-05 07:50:00')
);