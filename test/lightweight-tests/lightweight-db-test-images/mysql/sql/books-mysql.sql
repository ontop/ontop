DROP DATABASE IF EXISTS books;
CREATE DATABASE books;
USE books;

DROP TABLE IF EXISTS `books`;
CREATE TABLE `books` (
  `id` int(11) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `discount` decimal(10,2) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `lang` varchar(100) DEFAULT NULL,
  -- TIMESTAMP has a range of '1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' UTC.
  -- DATETIME should be used outside of this range
  `publication_date` timestamp,
  PRIMARY KEY (`id`)
);

-- We cannot add timezones when working with the datetime datatype in MySQL
-- Timezones can be added for TIMESTAMP MySQL v8.*, however MySQL always accounts for Daylight Savings Time, so consistency would be difficult
INSERT INTO `books` VALUES (1,'SPARQL Tutorial', 43, 0.2, 'good', 'en', '2014-06-05 16:47:52');
INSERT INTO `books` VALUES (2, 'The Semantic Web', 23, 0.25, 'bad', 'en', '2011-12-08 11:30:00');
INSERT INTO `books` VALUES (3, 'Crime and Punishment', 34, 0.2, 'good', 'en', '2015-09-21 09:23:06');
INSERT INTO `books` VALUES (4, 'The Logic Book: Introduction, Second Edition', 10, 0.15, 'good', 'en', '1970-11-05 07:50:00');

