CREATE TABLE data (
                      id integer PRIMARY KEY NOT NULL,
                      label character varying(10),
                      language character varying(10)
);
INSERT INTO data VALUES (1, 'english', 'en');
INSERT INTO data VALUES (2, 'french', 'fr');
INSERT INTO data VALUES (3, 'german', 'de');
INSERT INTO data VALUES (4, 'chinese', 'zh');