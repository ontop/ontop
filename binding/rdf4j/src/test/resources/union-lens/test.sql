CREATE TABLE BudgetYear2020(
    dayOfYear int primary key,
    spendings int not null,
    earnings int not null
);

INSERT INTO BudgetYear2020(dayOfYear, spendings, earnings) VALUES (1, 100, 40);
INSERT INTO BudgetYear2020(dayOfYear, spendings, earnings) VALUES (2, 0, 45);
INSERT INTO BudgetYear2020(dayOfYear, spendings, earnings) VALUES (3, 0, 60);
INSERT INTO BudgetYear2020(dayOfYear, spendings, earnings) VALUES (4, 20, 50);
INSERT INTO BudgetYear2020(dayOfYear, spendings, earnings) VALUES (5, 200, 40);
INSERT INTO BudgetYear2020(dayOfYear, spendings, earnings) VALUES (6, 50, 50);

CREATE TABLE BudgetYear2021(
    dayOfYear int primary key,
    spendings int not null,
    earnings int not null
);

INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (1, 10, 40);
INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (2, 10, 105);
INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (3, 40, 50);
INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (4, 20, 20);
INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (5, 30, 70);
INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (6, 50, 30);
INSERT INTO BudgetYear2021(dayOfYear, spendings, earnings) VALUES (7, 50, 45);

CREATE TABLE BudgetYear2022(
    dayOfYear int primary key,
    spendings int not null,
    earnings int not null,
    managerName varchar(100)
);

INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (1, 30, 80, 'Carl');
INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (2, 40, 125, 'Carl');
INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (3, 40, 70, 'John');
INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (4, 40, 70, 'John');
INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (5, 60, 90, 'Amanda');
INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (6, 55, 68, 'Carl');
INSERT INTO BudgetYear2022(dayOfYear, spendings, earnings, managerName) VALUES (7, 40, 72, 'Amanda');