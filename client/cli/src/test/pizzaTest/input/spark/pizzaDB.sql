DROP DATABASE IF EXISTS pizzaDB CASCADE;

CREATE DATABASE IF NOT EXISTS pizzaDB;
                          
CREATE TABLE pizzaDB.pizza (
          pizzaID STRING NOT NULL,
          menuName STRING NOT NULL,
          menuNumber INTEGER NOT NULL);
          
CREATE TABLE pizzaDB.sensor (
          sensorID STRING NOT NULL,
          measureType STRING NOT NULL,
          enabled BOOLEAN NOT NULL,
          installationDate DATE NOT NULL);
          
CREATE TABLE pizzaDB.measures (
          measureID STRING NOT NULL,
          pizzaID STRING NOT NULL,
          sensorID STRING NOT NULL,
          value DECIMAL(4,1) NOT NULL,
          timestamp TIMESTAMP NOT NULL);
          
INSERT INTO pizzaDB.measures
VALUES ('TEMP0001','PZ123','S1',250.5,CAST ('2020-10-01 23:54:46.15' AS Timestamp)),('TEMP0002','PZ222','S1',273.0,CAST ('2020-10-05 13:54:46.15' AS Timestamp)),('HUM0001','PZ222','S2',0.30,CAST ('2020-10-05 13:55:46.15' AS Timestamp)),('TEMP0003','PZ333','S1',380.0,CAST ('2020-10-02 23:54:46.15' AS Timestamp)),('TEMP0004','PZ444','S1',254.7,CAST ('2020-10-05 16:24:46.15' AS Timestamp)),('HUM0002','PZ444','S2',0.35,CAST ('2020-10-05 16:25:46.15' AS Timestamp)),('TEMP0005','PZ444','S3',258.9,CAST ('2020-10-05 16:26:46.15' AS Timestamp)),('TEMP0006','PZ456','S1',270.2,CAST ('2020-10-03 23:54:46.15' AS Timestamp)),('TEMP0007','PZ789','S1',260.9,CAST ('2020-10-04 23:54:46.15' AS Timestamp));

INSERT INTO pizzaDB.pizza
VALUES ('PZ123','Veneziana',24),('PZ222','Margherita',1),('PZ333','FourSeasons',14),('PZ444','Margherita',1),('PZ456','Margherita',1),('PZ789','FourSeasons',14);

INSERT INTO pizzaDB.sensor
VALUES ('S1','TEMPERATURE',true,CAST ('2020-10-02' AS DATE)),('S2','HUMIDITY',true,CAST ('2020-10-15' AS DATE)),('S3','TEMPERATURE',false,CAST ('2020-10-15' AS DATE));
