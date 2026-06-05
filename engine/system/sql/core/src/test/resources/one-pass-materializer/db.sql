CREATE TABLE employees (
    emp_id INT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    emp_type VARCHAR(20) NOT NULL,
    hire_date DATE,
    salary DECIMAL(10,2),
    CONSTRAINT emp_pk PRIMARY KEY (emp_id),
    CONSTRAINT first_name_unique UNIQUE (first_name));

CREATE TABLE departments (
    dept_id INT NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    budget DECIMAL(12,2),
    CONSTRAINT dept_pk PRIMARY KEY (dept_id),
    CONSTRAINT dept_name_unique UNIQUE (dept_name));

CREATE TABLE employee_dept (
    assignment_id INT NOT NULL,
    emp_id INT NOT NULL,
    dept_id INT NOT NULL,
    role VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE,
    CONSTRAINT emp_dept_pk PRIMARY KEY (assignment_id),
    CONSTRAINT emp_fk FOREIGN KEY (emp_id) REFERENCES employees(emp_id),
    CONSTRAINT dept_fk FOREIGN KEY (dept_id) REFERENCES departments(dept_id));

CREATE TABLE projects (
    project_id INT NOT NULL,
    project_name VARCHAR(100) NOT NULL,
    dept_id INT,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT proj_pk PRIMARY KEY (project_id),
    CONSTRAINT proj_dept_fk FOREIGN KEY (dept_id) REFERENCES departments(dept_id));

CREATE TABLE project_assignments (
    assignment_id INT NOT NULL,
    emp_id INT NOT NULL,
    project_id INT NOT NULL,
    hours_allocated DECIMAL(5,2),
    CONSTRAINT assign_pk PRIMARY KEY (assignment_id),
    CONSTRAINT assign_emp_fk FOREIGN KEY (emp_id) REFERENCES employees(emp_id),
    CONSTRAINT assign_proj_fk FOREIGN KEY (project_id) REFERENCES projects(project_id));

CREATE TABLE empty_table (
    id INT NOT NULL,
    data VARCHAR(100),
    CONSTRAINT empty_pk PRIMARY KEY (id));

CREATE TABLE null_table (
    id INT ,
    data VARCHAR(100));

INSERT INTO employees (emp_id, first_name, last_name, email, emp_type, hire_date, salary) VALUES
(1, 'Alice', 'Johnson', 'alice.johnson@example.com',  'MANAGER', '2020-01-15', 95000.00),
(2, 'Bob', 'Smith', NULL,  'ENGINEER', '2020-03-20', 75000.00),
(3, 'Charlie', 'Brown', 'charlie.brown@example.com',  'ENGINEER', '2021-06-10', 72000.00),
(4, 'Diana', 'Martinez', NULL,  'SALES', '2019-11-05', 68000.00);

INSERT INTO departments (dept_id, dept_name, location, budget) VALUES
(10, 'Engineering', 'Building A', 500000.00),
(20, 'Sales', 'Building B', NULL),
(30, 'Management', 'Building B', 200000.00);

INSERT INTO employee_dept (assignment_id, emp_id, dept_id, role, start_date, end_date) VALUES
(1,1, 30, 'Department Head', '2020-01-15', NULL),
(2,1, 10, 'Technical Lead', '2020-01-15', '2020-12-31'),
(3,2, 10, 'Senior Engineer', '2020-03-20', NULL),
(4,3, 10, NULL, '2021-06-10', NULL),
(5,4, 20, 'Sales Rep', '2019-11-05', NULL);

INSERT INTO projects (project_id, project_name, dept_id, start_date, end_date, status) VALUES
(100, 'Cloud Migration', 10, '2022-01-01', '2023-06-30', 'COMPLETED'),
(101, 'Mobile App', 10, '2023-01-15', NULL, 'ACTIVE'),
(102, 'Sales Campaign', 20, '2023-03-01', '2023-12-31', 'ACTIVE'),
(103, 'Market Research', NULL, '2023-06-01', NULL, 'ACTIVE'),
(104, 'Legacy System', 10, '2019-01-01', '2021-12-31', 'CANCELLED');

INSERT INTO project_assignments (assignment_id, emp_id, project_id, hours_allocated) VALUES
(1001, 2, 100, 160.00),
(1005, 3, 101, NULL),
(1007, 4, 102, 180.00),
(1009, 1, 103, NULL);

INSERT INTO null_table (id, data) VALUES
(NULL, NULL);