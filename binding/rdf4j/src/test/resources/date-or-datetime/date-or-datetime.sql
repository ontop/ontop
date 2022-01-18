CREATE TABLE visit_occurrence (
                      visit_occurrence_id integer PRIMARY KEY NOT NULL,
                      person_id integer NOT NULL,
                      visit_concept_id integer NOT NULL,
                      visit_start_date date NOT NULL,
                      visit_start_datetime timestamp,
                      visit_end_date date NOT NULL,
                      visit_end_datetime timestamp,
                      visit_type_concept_id integer NOT NULL

);

INSERT INTO visit_occurrence VALUES (1, 1, 1, '1949-12-17', '1949-12-17 11:10:00.000000', '1949-12-31', '1949-12-31 14:55:00.000000', 1);
INSERT INTO visit_occurrence VALUES (2, 1, 1, '1955-02-03', '1955-02-03 17:43:00.000000', '1955-02-14', '1955-02-14 11:15:00.000000', 1);
