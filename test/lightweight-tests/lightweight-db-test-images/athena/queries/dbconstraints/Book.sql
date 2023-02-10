CREATE EXTERNAL TABLE IF NOT EXISTS Book (
  bk_code int,
  bk_title varchar(100)
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION 's3://${athena.bucket}/data/dbconstraints/Book';