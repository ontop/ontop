CREATE EXTERNAL TABLE IF NOT EXISTS books (
  id int, 
  title varchar(100), 
  price int, 
  discount decimal(38, 2), 
  description varchar(100), 
  lang varchar(100), 
  publication_date timestamp
) 
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
LOCATION 's3://${athena.bucket}/data/books';