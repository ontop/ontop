CREATE EXTERNAL TABLE IF NOT EXISTS teaching (
  course_id varchar(100), 
  prof_id int
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION '${athena.bucket}/data/university/teaching';