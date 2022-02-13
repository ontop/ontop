CREATE TABLE condition_occurrence (
    condition_occurrence_id integer NOT NULL,
    person_id integer NOT NULL,
    condition_concept_id integer NOT NULL,
    condition_start_datetime timestamp
);

INSERT INTO condition_occurrence VALUES (1, 1984, 2, '1949-12-31 14:55:00.000000');
INSERT INTO condition_occurrence VALUES (2, 1984, 2, '1949-12-31 14:55:00.000000');

CREATE TABLE concept (
    concept_id integer NOT NULL,
    concept_code text NOT NULL
);

INSERT INTO concept VALUES (1, '403');
INSERT INTO concept VALUES (2, '404');