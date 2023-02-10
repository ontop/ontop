CREATE EXTERNAL TABLE IF NOT EXISTS course (
  course_id varchar(100), 
  nb_students int, 
  duration decimal(38, 4)
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION 's3://${athena.bucket}/data/university/course';