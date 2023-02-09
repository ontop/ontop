CREATE EXTERNAL TABLE IF NOT EXISTS professors (
  prof_id int, 
  first_name varchar(100), 
  last_name varchar(100),
  nickname varchar(100)
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION '${athena.bucket}/data/university/professors';