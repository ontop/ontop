CREATE EXTERNAL TABLE IF NOT EXISTS Writer (
  wr_code int,
  wr_name varchar(100)
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION 's3://${athena.bucket}/data/dbconstraints/Writer';