CREATE EXTERNAL TABLE IF NOT EXISTS BookWriter (
  bk_code int,
  wr_code int
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION 's3://${athena.bucket}/data/dbconstraints/BookWriter';