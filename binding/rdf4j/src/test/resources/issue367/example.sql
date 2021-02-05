DROP TABLE IF EXISTS MesData;
CREATE TABLE MesData (shopOrder INT);

DROP TABLE IF EXISTS Temperatures;
CREATE TABLE Temperatures (temp_type VARCHAR(100));

INSERT INTO MesData (shopOrder) VALUES (1);
INSERT INTO Temperatures (temp_type) VALUES ('temp1');
