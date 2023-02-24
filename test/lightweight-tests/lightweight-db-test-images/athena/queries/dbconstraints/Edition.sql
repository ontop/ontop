CREATE EXTERNAL TABLE IF NOT EXISTS Edition (
  ed_code int,
  ed_year int,
  bk_code int
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION 's3://${athena.bucket}/data/dbconstraints/Edition';