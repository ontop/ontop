create table professors (
	prof_id int primary key,
	first_name varchar(100) NOT NULL,
	last_name varchar(100) NOT NULL,
    age tinyint,
    worked_minutes bigint,
    worked_hours int,
    salary_per_hour int,
    float_average_salary_per_minute real,
    numeric_average_salary_per_minute numeric,
    double_avargae_salary_per_minute double precision,
    birthday_without_timezone TIMESTAMP,
    birthday_with_timezone TIMESTAMP WITH TIME ZONE
);

insert into professors  (prof_id, first_name, last_name, age, worked_minutes, worked_hours, salary_per_hour, float_average_salary_per_minute, numeric_average_salary_per_minute, double_avargae_salary_per_minute, birthday_with_timezone)
values  (10,      'Roger',     'Smith',   42,  2448000,        40800,        23,               0.3833333,                           0.3833333,                             0.3833333,                             '1981-06-04 14:41:05 +02:00');
insert into professors  (prof_id, first_name, last_name, age, worked_minutes, worked_hours, salary_per_hour, float_average_salary_per_minute, numeric_average_salary_per_minute, double_avargae_salary_per_minute, birthday_with_timezone)
values  (20,      'John',     ' Winner',  38,  1075200,        17920,        17,               0.28333,                           0.28333,                            0.28333,                             '1991-06-04 14:41:05 +02:00');
insert into professors  (prof_id, first_name, last_name, age, worked_minutes, worked_hours, salary_per_hour, float_average_salary_per_minute, numeric_average_salary_per_minute, double_avargae_salary_per_minute, birthday_with_timezone)
values  (30,      'Diego',     'Muler',   24,  326400,        40800,        16,               0.26,                           0.26,                             0.26,                             '2001-06-04 14:41:05 +02:00');

