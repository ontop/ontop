DROP SCHEMA IF EXISTS "pizzaDB";

CREATE SCHEMA "pizzaDB";

CREATE TABLE "pizzaDB"."measures" (	"measureID" VARCHAR(10) NOT NULL,
					"pizzaID" VARCHAR(10) NOT NULL,
					"sensorID" VARCHAR(10) NOT NULL,
					"value" DECIMAL(4,1) NOT NULL,
					"timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
					PRIMARY KEY ("measureID")
				      );
				
CREATE TABLE "pizzaDB"."pizza" ( "pizzaID" VARCHAR(10) NOT NULL,
				  "menuName" VARCHAR(30) NOT NULL,
				  "menuNumber" INTEGER NOT NULL,
				  PRIMARY KEY ("pizzaID")
				);
				 
CREATE TABLE "pizzaDB"."sensor" ( "sensorID" VARCHAR(10) NOT NULL,
				   "measureType" VARCHAR(30) NOT NULL,
				   "enabled" BOOLEAN NOT NULL,
				   "installationDate" DATE NOT NULL,
				   PRIMARY KEY ("sensorID")
				  );				 				 
				  
INSERT INTO "pizzaDB"."measures"("measureID","pizzaID","sensorID","value","timestamp")
VALUES ('TEMP0001','PZ123','S1',250.5,'2020-10-01 23:54:46.15'),('TEMP0002','PZ222','S1',273.0,'2020-10-05 13:54:46.15'),('HUM0001','PZ222','S2',0.30,'2020-10-05 13:55:46.15'),('TEMP0003','PZ333','S1',380.0,'2020-10-02 23:54:46.15'),('TEMP0004','PZ444','S1',254.7,'2020-10-05 16:24:46.15'),('HUM0002','PZ444','S2',0.35,'2020-10-05 16:25:46.15'),('TEMP0005','PZ444','S3',258.9,'2020-10-05 16:26:46.15'),('TEMP0006','PZ456','S1',270.2,'2020-10-03 23:54:46.15'),('TEMP0007','PZ789','S1',260.9,'2020-10-04 23:54:46.15');

INSERT INTO "pizzaDB"."pizza"("pizzaID","menuName","menuNumber")
VALUES ('PZ123','Veneziana',24),('PZ222','Margherita',1),('PZ333','FourSeasons',14),('PZ444','Margherita',1),('PZ456','Margherita',1),('PZ789','FourSeasons',14);

INSERT INTO "pizzaDB"."sensor"("sensorID","measureType","enabled","installationDate")
VALUES ('S1','TEMPERATURE',TRUE,'2020-10-02'),('S2','HUMIDITY',TRUE,'2020-10-15'),('S3','TEMPERATURE',FALSE,'2020-10-15');

