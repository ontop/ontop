DROP TABLE Professors IF EXISTS;
DROP TABLE Lecturers IF EXISTS;
DROP TABLE Chairs IF EXISTS;
DROP TABLE SupportStaff IF EXISTS;

---

CREATE TABLE Professors (
deptID integer NOT NULL, 
uniID integer NOT NULL, 
profID integer NOT NULL,
profType varchar(200) NOT NULL,
isWorking integer NOT NULL,
isHeadOfDep integer NOT NULL
);

---

CREATE TABLE Lecturers (
deptID integer NOT NULL, 
uniID integer NOT NULL,
lecturerID integer NOT NULL,
isWorking integer NOT NULL
);

---

CREATE TABLE Chairs (
deptID integer NOT NULL, 
uniID integer NOT NULL, 
chairID integer NOT NULL
);

---

CREATE TABLE SupportStaff (
deptID integer NOT NULL, 
uniID integer NOT NULL, 
ssID integer NOT NULL, 
staffType varchar(200) NOT NULL
);

---