DROP TABLE IF EXISTS mytable;
CREATE TABLE mytable (
                           "id" integer primary key,
                           "length" decimal(20,2),
                           "type" text);

insert into mytable ("id", "length", "type")
values
    (1, 123.5, 'A'),
    (2, 1234.1, 'A'),
    (3, 122.67, 'B'),
    (4, 1214.678, 'C');
